package org.example.event_platform.Service.Auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.example.event_platform.Dto.Auth.LoginRequest;
import org.example.event_platform.Dto.Auth.UserResponse; // Lưu ý: Ông có thể cần 1 DTO chung hoặc check role ở FE
//import org.example.event_platform.Entity.AdminPlatform;
import org.example.event_platform.Entity.User;
import org.example.event_platform.Mapper.UserMapper;
//import org.example.event_platform.Repository.AdminPlatformRepository;
import org.example.event_platform.Repository.AuthRepository;
import org.example.event_platform.Security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
//    private final AdminPlatformRepository adminPlatformRepository;
    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(AuthRepository authRepository,
                           UserMapper userMapper,
                           JwtTokenProvider jwtTokenProvider,
                           @org.springframework.context.annotation.Lazy AuthenticationManager authenticationManager,
                           PasswordEncoder passwordEncoder) {
        this.authRepository = authRepository;
        this.userMapper = userMapper;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Object login(LoginRequest dto, HttpServletResponse response) {
        // 1. Tìm user duy nhất từ bảng chung
        User user = authRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("Tài khoản hoặc mật khẩu không chính xác"));

        // 2. Kiểm tra mật khẩu
        validatePassword(dto.getPassword(), user.getPassword());

        // 3. Kiểm tra trạng thái tài khoản cá nhân
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa.");
        }

        // 4. Kiểm tra trạng thái Tenant (Chỉ check nếu KHÔNG PHẢI chủ sàn)
        // Chủ sàn (SUPER_ADMIN) thường có tenant == null
        if (user.getTenant() != null && !user.getTenant().isActive()) {
            throw new RuntimeException("Đơn vị (Tenant) của bạn đã bị ngừng hoạt động hoặc hết hạn.");
        }

        // 5. Tạo Token (Dùng hàm duy nhất đã sửa ở trên)
        String token = jwtTokenProvider.generateToken(user);

        // 6. Set Cookie
        setAuthCookie(response, token);

        // 7. Trả về Response dựa trên Role (Hoặc dùng chung 1 DTO tùy ông)
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Object getMyInfo() {
        // 1. Lấy thông tin từ Context (Đã được nạp ở Filter)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("Chưa đăng nhập hoặc phiên làm việc hết hạn");
        }

        Object principal = auth.getPrincipal();

        // 2. Kiểm tra danh tính dựa trên Instance (Không cần Query DB lại)
//        if (principal instanceof org.example.event_platform.Entity.AdminPlatform admin) {
//            return adminPlatformMapper.toResponse(admin);
//        }

        if (principal instanceof org.example.event_platform.Entity.User user) {
            return userMapper.toResponse(user);
        }

        // Trường hợp dự phòng nếu principal chỉ là username (String)
        throw new RuntimeException("Dữ liệu danh tính không hợp lệ trong Security Context");
    }

    @Override
    public void logout(HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        clearAuthCookie(response);
    }

    // --- Helper Methods ---

    private void validatePassword(String raw, String encoded) {
        if (!passwordEncoder.matches(raw, encoded)) {
            throw new RuntimeException("Tài khoản hoặc mật khẩu không chính xác");
        }
    }

    private void setAuthCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("access_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        cookie.setAttribute("SameSite", "None");
        cookie.setAttribute("Partitioned", "");
        response.addCookie(cookie);
    }

    private void clearAuthCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("access_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "None");
        cookie.setAttribute("Partitioned", "");
        response.addCookie(cookie);
    }
}