package org.example.event_platform.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.event_platform.Dto.Auth.ChangePasswordRequest;
import org.example.event_platform.Dto.Auth.CreateUserRequest;
import org.example.event_platform.Dto.Auth.UserResponse;
import org.example.event_platform.Dto.Auth.UserUpdateRequest;
import org.example.event_platform.Entity.User;
import org.example.event_platform.Service.User.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private final UserService userService;

    // 1. Lấy danh sách User (Có phân trang)
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<Page<UserResponse>> getUsers(
            @PageableDefault(size = 10, sort = "id") Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.getUsers(pageable, currentUser));
    }

    // 2. Tạo User mới
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest dto,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(dto, currentUser));
    }

    // 3. Cập nhật User
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest dto,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.updateUser(id, dto, currentUser));
    }

    // 4. Đổi mật khẩu (Cho cả Admin reset hoặc User tự đổi)
    @PatchMapping("/{id}/change-password")
    public ResponseEntity<String> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest dto,
            @AuthenticationPrincipal User currentUser) {
        userService.changePassword(id, dto, currentUser);
        return ResponseEntity.ok("Đổi mật khẩu thành công!");
    }

    // 5. API lẻ để gán quyền nhanh
    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<String> assignPermissions(
            @PathVariable Long id,
            @RequestBody Set<Long> permissionIds,
            @AuthenticationPrincipal User currentUser) {
        userService.assignPermissions(id, permissionIds, currentUser);
        return ResponseEntity.ok("Cấp quyền thành công!");
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserDetail(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserDetail(id));
    }

    @GetMapping("/{userId}/my-stats")
    public ResponseEntity<Map<String, Object>> getMyStats(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getMemberStats(userId));
    }
}
