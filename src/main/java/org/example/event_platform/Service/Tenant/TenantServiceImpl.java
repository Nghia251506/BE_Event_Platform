package org.example.event_platform.Service.Tenant;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.example.event_platform.Dto.Tenant.TenantRequest;
import org.example.event_platform.Dto.Tenant.TenantResponse;
import org.example.event_platform.Entity.*;
import org.example.event_platform.Mapper.TenantMapper;
import org.example.event_platform.Repository.RoleRepository;
import org.example.event_platform.Repository.TenantRepository;
import org.example.event_platform.Repository.UserRepository;
import org.example.event_platform.Service.EmailService;
import org.example.event_platform.Service.Tenant.TenantService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantMapper tenantMapper;
    private final PasswordEncoder passwordEncoder; // Dùng BCryptPasswordEncoder
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    @Transactional
    public TenantResponse registerTenant(TenantRequest request) {
        // 1. Validate sơ bộ
        if (tenantRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại trên hệ thống!");
        }
        if (tenantRepository.existsByDomain(request.getDomain())) {
            throw new RuntimeException("Domain/Slug này đã được sử dụng!");
        }

        // 2. Tạo Tenant (Đội lân/Cty sự kiện)
        Tenant tenant = tenantMapper.toEntity(request);
        tenant.setVerificationToken(UUID.randomUUID().toString());
        tenant.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        tenant.setIsVerified(false);
        tenant.setStatusConfirm(RegistrationStatus.PENDING_VERIFICATION);
        tenant.setActive(true);

        tenant = tenantRepository.save(tenant);

        // 3. Tạo tài khoản Admin mặc định cho Tenant này
        User adminUser = new User();
        adminUser.setTenant(tenant);
        adminUser.setUsername(request.getEmail()); // Dùng email làm username luôn cho tiện
        adminUser.setEmail(request.getEmail());
        adminUser.setFullName("Admin");
        adminUser.setIsVerified(false);
        adminUser.setIsActive(false);
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser.setSeniority(0); // Admin mới khởi tạo

        // Tạo mật khẩu tạm thời
        String tempPassword = RandomStringUtils.randomAlphanumeric(10);
        adminUser.setPassword(passwordEncoder.encode(tempPassword));

        // Gán Role ADMIN (Nhớ tạo role này trong DB trước nhé)
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("Lỗi hệ thống: Không tìm thấy quyền Admin"));
        adminUser.setRoles(adminRole);

        userRepository.save(adminUser);

        // 4. Lưu mật khẩu tạm vào Redis để sau này xác thực xong thì gửi lại cho khách hoặc đối chiếu
        // Key: temp_pwd:tenantId
        redisTemplate.opsForValue().set(
                "temp_password:" + tenant.getId(),
                tempPassword,
                24, TimeUnit.HOURS
        );

        // 5. Gửi email xác thực
        emailService.sendVerificationEmail(
                tenant.getEmail(),
                tenant.getVerificationToken(),
                tenant.getId()
        );

        // 6. Trả về response (không kèm pass)
        TenantResponse response = tenantMapper.toResponse(tenant);
        // Có thể set thêm username mặc định để FE hiển thị cho người dùng biết
        return response;
    }

    @Override
    @Transactional
    public void verifyTenant(String token) {
        // 1. Tìm và check token (Giữ nguyên của ông)
        Tenant tenant = tenantRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ hoặc đã được sử dụng"));

        if (tenant.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Link xác thực đã hết hạn (24h)!");
        }

        // 2. Kích hoạt Tenant (Giữ nguyên của ông)
        tenant.setIsVerified(true);
        tenant.setStatusConfirm(RegistrationStatus.ACTIVE);
        tenant.setVerificationToken(null);
        tenant.setVerificationTokenExpiry(null);
        tenantRepository.save(tenant);

        // 3. Kích hoạt User Admin và gửi Mail (Thêm logic gửi credentials)
        userRepository.findByTenantIdAndEmail(tenant.getId(), tenant.getEmail())
                .ifPresent(u -> {
                    u.setIsVerified(true);
                    // Cập nhật trạng thái confirm cho user luôn cho đồng bộ
                    u.setStatus(UserStatus.ACTIVE);
                    userRepository.save(u);

                    // Lấy mật khẩu tạm từ Redis
                    String pwd = redisTemplate.opsForValue().get("temp_password:" + tenant.getId());

                    if (pwd != null) {
                        // Gọi hàm gửi mail thông tin đăng nhập mà ông đã viết trong EmailService
                        emailService.sendCredentialsEmail(tenant.getEmail(), u.getUsername(), pwd);

                        // Gửi xong thì xóa key trong redis cho bảo mật
                        redisTemplate.delete("temp_password:" + tenant.getId());
                    } else {
                        // Trường hợp redis hết hạn (quá 24h) mà khách mới bấm verify
                        // Ông có thể log warning hoặc bắn exception tùy ý
                        System.out.println("Cảnh báo: Không tìm thấy mật khẩu tạm trong Redis cho Tenant ID: " + tenant.getId());
                    }
                });
    }

    @Override
    public TenantResponse getTenantById(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn vị với ID: " + id));
        return tenantMapper.toResponse(tenant);
    }

    @Override
    public List<TenantResponse> getAllTenants() {
        return tenantRepository.findAll().stream()
                .map(tenantMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TenantResponse updateTenant(Long id, TenantRequest request) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn vị"));

        // Dùng MapStruct để update các field từ request vào entity hiện tại
        tenantMapper.updateTenantFromRequest(request, tenant);

        return tenantMapper.toResponse(tenantRepository.save(tenant));
    }

    @Override
    @Transactional
    public void deleteTenant(Long id) {
        // Thay vì xóa cứng, ta thường chỉ deactivate để giữ dữ liệu lịch sử
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn vị"));
        tenant.setActive(false);
        tenantRepository.save(tenant);
    }
}