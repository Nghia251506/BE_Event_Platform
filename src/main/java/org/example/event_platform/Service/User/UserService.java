package org.example.event_platform.Service.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.event_platform.Dto.Auth.ChangePasswordRequest;
import org.example.event_platform.Dto.Auth.CreateUserRequest;
import org.example.event_platform.Dto.Auth.UserResponse;
import org.example.event_platform.Dto.Auth.UserUpdateRequest;
import org.example.event_platform.Entity.*;
import org.example.event_platform.Mapper.UserMapper;
import org.example.event_platform.Repository.PermissionRepository;
import org.example.event_platform.Repository.RoleRepository;
import org.example.event_platform.Repository.UserEventRepository;
import org.example.event_platform.Repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserEventRepository userEventRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // 1. LẤY DANH SÁCH USER (PHÂN TRANG)
    @Transactional
    public Page<UserResponse> getUsers(Pageable pageable, User currentUser) {
        Page<User> users;
        // Nếu là SUPER_ADMIN -> Thấy tất cả user trên sàn
        if (isSuperAdmin(currentUser)) {
            users = userRepository.findAll(pageable);
        } else {
            // Nếu là Admin đội -> Chỉ thấy user thuộc đội của mình
            users = userRepository.findByTenantId(currentUser.getTenant().getId(), pageable);
        }
        return users.map(userMapper::toResponse);
    }

    // 2. TẠO USER MỚI
    @Transactional
    public UserResponse createUser(CreateUserRequest dto, User currentUser) {
        // 1. Check trùng username
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username đã tồn tại");
        }

        // 2. Map DTO sang Entity
        User newUser = userMapper.toEntity(dto);
        newUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        newUser.setStatus(UserStatus.ACTIVE);
        newUser.setIsActive(true);
        newUser.setIsVerified(true);

        // 3. Gán Role
        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role không tồn tại"));
        newUser.setRoles(role);

        // 4. LOGIC GÁN TENANT TỰ ĐỘNG (Chốt chặn ở đây)
        if (isSuperAdmin(currentUser)) {
            // Nếu là CHỦ SÀN: Mặc định tenant là null (User hệ thống)
            // Nếu sau này ông muốn chủ sàn tạo hộ user cho đội khác thì mới cần thêm field vào DTO
            newUser.setTenant(null);
            log.info("Chủ sàn tạo tài khoản hệ thống: {}", dto.getUsername());
        } else {
            // Nếu là CHỦ ĐỘI: Ép buộc lấy Tenant ID từ chính chủ đội
            if (currentUser.getTenant() == null) {
                throw new RuntimeException("Lỗi: Tài khoản của bạn không thuộc đơn vị nào để thực hiện gán!");
            }
            newUser.setTenant(currentUser.getTenant());
            log.info("Chủ đội {} tạo nhân viên mới: {}", currentUser.getTenant().getName(), dto.getUsername());
        }

        // 5. Lưu và trả về
        return userMapper.toResponse(userRepository.save(newUser));
    }

    // 3. CẬP NHẬT USER
    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest dto, User currentUser) {
        User targetUser = findUserSafe(userId, currentUser);

        userMapper.updateEntity(targetUser,dto);

        // Cập nhật Role nếu có gửi roleId
        if (dto.getRoleId() != null) {
            Role role = roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role không tồn tại"));
            targetUser.setRoles(role);
        }

        // Cập nhật quyền gán trực tiếp nếu có
        if (dto.getPermissionIds() != null) {
            Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(dto.getPermissionIds()));
            targetUser.setPermissions(permissions);
        }

        return userMapper.toResponse(userRepository.save(targetUser));
    }

    @Transactional(readOnly = true) // Thêm cái này để tránh lỗi Lazy Loading khi chạm vào permissions
    public UserResponse getUserDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setIsActive(user.getIsActive());
        response.setIsVerified(user.getIsVerified());

        if (user.getTenant() != null) {
            response.setTenantId(user.getTenant().getId());
            response.setTenantName(user.getTenant().getName());
        }

        Set<String> allPermissions = new HashSet<>();

        // 1. Lấy quyền từ Role
        if (user.getRoles() != null) {
            response.setRoleName(user.getRoles().getName());
            if (user.getRoles().getPermissions() != null) {
                user.getRoles().getPermissions().forEach(p -> allPermissions.add(p.getName()));
            }
        }

        // 2. Lấy quyền "Direct" (Cái này là cái ông đang thiếu nè!)
        if (user.getPermissions() != null) {
            user.getPermissions().forEach(p -> allPermissions.add(p.getName()));
        }

        response.setPermissions(allPermissions);
        return response;
    }

    // 4. CẤP QUYỀN TRỰC TIẾP (API LẺ)
    @Transactional
    public void assignPermissions(Long userId, Set<Long> permissionIds, User currentUser) {
        User targetUser = findUserSafe(userId, currentUser);

        // 1. Lấy danh sách object Permission từ DB dựa trên list ID gửi lên
        List<Permission> newPermissions = permissionRepository.findAllById(permissionIds);

        // 2. Chốt chặn bảo mật: Nếu là ADMIN Đội, chỉ được gán quyền có tiền tố TENANT_
        // và thuộc về Tenant của mình (hoặc quyền GLOBAL được phép)
        if (!"SUPER_ADMIN".equals(currentUser.getRoles().getName())) {
            for (Permission p : newPermissions) {
                if (p.getTenant() != null && !p.getTenant().getId().equals(currentUser.getTenant().getId())) {
                    throw new AccessDeniedException("Ông định gán quyền của đội khác cho nhân viên à? Không dễ thế đâu!");
                }
            }
        }

        // 3. Logic Cộng dồn (Nếu FE chỉ gửi quyền bổ sung)
        // targetUser.getPermissions().addAll(new HashSet<>(newPermissions));

        // 4. Logic Đồng bộ (FE gửi toàn bộ list cần có - Cách này an toàn nhất cho việc gỡ quyền)
        targetUser.setPermissions(new HashSet<>(newPermissions));

        userRepository.save(targetUser);
    }

    // 5. ĐỔI MẬT KHẨU
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest dto, User currentUser) {
        User targetUser = findUserSafe(userId, currentUser);

        // Kiểm tra logic xác nhận mật khẩu
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Xác nhận mật khẩu không khớp");
        }

        // Nếu user tự đổi mật khẩu cho mình -> bắt nhập pass cũ
        if (currentUser.getId().equals(targetUser.getId())) {
            if (!passwordEncoder.matches(dto.getOldPassword(), targetUser.getPassword())) {
                throw new RuntimeException("Mật khẩu cũ không chính xác");
            }
        }

        targetUser.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(targetUser);
    }

    // --- HÀM TIỆN ÍCH NỘI BỘ ---

    private boolean isSuperAdmin(User user) {
        return user.getRoles() != null && "SUPER_ADMIN".equals(user.getRoles().getName());
    }

    /**
     * Hàm quan trọng nhất: Đảm bảo Admin đội không can thiệp được vào User của đội khác
     */
    private User findUserSafe(Long userId, User currentUser) {
        if (isSuperAdmin(currentUser)) {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng ID: " + userId));
        } else {
            // Dùng hàm findByIdAndTenantId ông đã viết trong Repo
            return userRepository.findByIdAndTenantId(userId, currentUser.getTenant().getId())
                    .orElseThrow(() -> new RuntimeException("Người dùng không thuộc đơn vị quản lý của bạn"));
        }
    }

    public Map<String, Object> getMemberStats(Long userId) {
        long totalShows = userEventRepository.countCompletedShows(userId, AssignStatus.COMPLETED);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalShows", totalShows);
        // Sau này ông có thể thêm các chỉ số như: totalHours, totalMoney...
        return stats;
    }
}
