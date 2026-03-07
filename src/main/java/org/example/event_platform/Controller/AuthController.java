package org.example.event_platform.Controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.event_platform.Dto.Auth.LoginRequest;
import org.example.event_platform.Dto.Auth.UserResponse;
import org.example.event_platform.Service.Auth.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 1. Đăng nhập - Trả về User info và set Cookie qua HttpServletResponse
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {

        Object userResponse = authService.login(loginRequest, response);
        return ResponseEntity.ok(userResponse);
    }

    // 2. Lấy thông tin cá nhân - FE chỉ cần gọi API này, BE tự lấy từ Context thông qua Token trong Cookie
    @GetMapping("/me")
    public ResponseEntity<?> getMe() {
        return ResponseEntity.ok(authService.getMyInfo());
    }

    // 3. Đăng xuất - Xóa Cookie
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok("Đã đăng xuất thành công!");
    }
}