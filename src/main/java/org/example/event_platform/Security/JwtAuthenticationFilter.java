package org.example.event_platform.Security;

import org.example.event_platform.util.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//@Component
@Slf4j // Dùng cái này thay cho System.out cho nó "nghệ" ông nhé
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        if (!requestURI.contains("/h2-console") && !requestURI.contains("/favicon.ico")) {
            log.info("Processing request: {} {}", request.getMethod(), requestURI);
        }

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(jwt);

                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    Object principal = authentication.getPrincipal();

                    // Check kỹ xem principal có đúng là Entity User không
                    if (principal instanceof org.example.event_platform.Entity.User user) {
                        if (user.getTenant() != null) {
                            TenantContext.setCurrentTenantId(user.getTenant().getId());
                            log.info("Set Tenant ID: {} cho user: {}", user.getTenant().getId(), user.getUsername());
                        } else {
                            // Đây là SUPER_ADMIN hoặc Global User
                            TenantContext.setCurrentTenantId(null);
                            log.info("User hệ thống (Global): {}", user.getUsername());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Luôn dọn dẹp ThreadLocal
            TenantContext.clear();
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        // 1. Check Header (Ưu tiên hàng đầu)
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 2. Check Cookies (Dành cho FE gọi API từ trình duyệt)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // 3. Check Query Param (Dùng cho link ảnh/download nếu cần)
        String paramToken = request.getParameter("token");
        if (StringUtils.hasText(paramToken)) {
            return paramToken;
        }

        return null;
    }
}