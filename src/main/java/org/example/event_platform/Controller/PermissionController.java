package org.example.event_platform.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.event_platform.Dto.Permistion.PermissionRequest;
import org.example.event_platform.Dto.Permistion.PermissionResponse;
import org.example.event_platform.Entity.User;
import org.example.event_platform.Service.Auth.PermissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    // 1. Lấy danh sách quyền có sẵn (Dựa theo Role và Tenant của người dùng)
    @GetMapping
// Chỉ cho phép những người đã xác thực mới được gọi vào đây
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PermissionResponse>> getAllPermissions(
            @AuthenticationPrincipal User currentUser) {

        // Bảo vệ thêm một lớp nếu Principal vì lý do nào đó bị null
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

//        log.info("User {} đang truy vấn danh sách quyền", currentUser.getUsername());

        List<PermissionResponse> permissions = permissionService.getAllAvailablePermissions();
        return ResponseEntity.ok(permissions);
    }

    // 2. Tạo mới quyền (Chỉ SUPER_ADMIN và ADMIN đội)
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<PermissionResponse> createPermission(
            @Valid @RequestBody PermissionRequest request,
            @AuthenticationPrincipal User currentUser) {
        return new ResponseEntity<>(permissionService.createPermission(request, currentUser), HttpStatus.CREATED);
    }

    // 3. Cập nhật quyền
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<PermissionResponse> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody PermissionRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(permissionService.updatePermission(id, request, currentUser));
    }

    // 4. Xóa quyền
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<Void> deletePermission(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        permissionService.deletePermission(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}