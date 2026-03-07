package org.example.event_platform.Service.Auth;
import jakarta.servlet.http.HttpServletResponse;
import org.example.event_platform.Dto.Auth.LoginRequest;
import org.example.event_platform.Dto.Auth.UserResponse;

public interface AuthService {
    // Đăng nhập và trả về Token (Ở đây tôi giả định ông sẽ dùng JwtResponse để bọc Token)
    Object login(LoginRequest request,HttpServletResponse response);

    // Lấy thông tin user hiện tại từ Token
    Object getMyInfo();
    void logout(HttpServletResponse response);
}
