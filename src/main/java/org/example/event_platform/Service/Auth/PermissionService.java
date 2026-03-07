package org.example.event_platform.Service.Auth;

import lombok.RequiredArgsConstructor;
import org.example.event_platform.Dto.Permistion.PermissionRequest;
import org.example.event_platform.Dto.Permistion.PermissionResponse;
import org.example.event_platform.Entity.Permission;
import org.example.event_platform.Entity.User;
import org.example.event_platform.Repository.PermissionRepository;
import org.example.event_platform.Repository.TenantRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    // Cần thêm TenantRepository để load object tenant xịn tránh lỗi Hibernate
    private final TenantRepository tenantRepository;

    @Transactional
    public PermissionResponse createPermission(PermissionRequest request, User currentUser) {
        if (!"SUPER_ADMIN".equals(currentUser.getRoles().getName())) {
            throw new AccessDeniedException("Chỉ SUPER_ADMIN mới có quyền định nghĩa quyền hệ thống!");
        }

        Permission permission = new Permission();
        // Không cần prefix TENANT_ hay GLOBAL_ nữa, cứ để tên chuẩn nghiệp vụ
        // Ví dụ: EVENT_VIEW, USER_MANAGE, v.v.
        permission.setName(request.getName().toUpperCase().replace(" ", "_"));
        permission.setDescription(request.getDescription());

        // Bảng Permission giờ không còn cột tenant_id nữa nên không cần set

        return mapToResponse(permissionRepository.save(permission));
    }

    public List<PermissionResponse> getAllAvailablePermissions() {
        return permissionRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public PermissionResponse updatePermission(Long id, PermissionRequest request, User currentUser) {
        if (!"SUPER_ADMIN".equals(currentUser.getRoles().getName())) {
            throw new AccessDeniedException("Từ chối truy cập!");
        }

        Permission p = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quyền"));

        p.setName(request.getName().toUpperCase().replace(" ", "_"));
        p.setDescription(request.getDescription());

        return mapToResponse(permissionRepository.save(p));
    }

    @Transactional
    public void deletePermission(Long id, User currentUser) {
        Permission p = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quyền này"));

        validateOwnership(p, currentUser);
        permissionRepository.delete(p);
    }

    // --- Helper Methods ---

    private void validateOwnership(Permission p, User currentUser) {
        // 1. SUPER_ADMIN luôn có quyền
        if ("SUPER_ADMIN".equals(currentUser.getRoles().getName())) return;

        // 2. Nếu là ADMIN thường
        if (p.getTenant() == null) {
            // ADMIN thường không được sửa quyền Global (null)
            throw new AccessDeniedException("Chỉ chủ sàn mới được sửa quyền hệ thống!");
        }

        // 3. So sánh ID của Tenant (Check null an toàn)
        Long pTenantId = p.getTenant().getId();
        Long userTenantId = (currentUser.getTenant() != null) ? currentUser.getTenant().getId() : null;

        if (userTenantId == null || !pTenantId.equals(userTenantId)) {
            throw new AccessDeniedException("Ông không có quyền đụng vào Permission của đội khác!");
        }
    }

    private PermissionResponse mapToResponse(Permission p) {
        return PermissionResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .build();
    }
}
