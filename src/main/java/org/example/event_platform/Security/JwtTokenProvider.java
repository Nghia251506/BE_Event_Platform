package org.example.event_platform.Security;

//import org.example.event_platform.Entity.AdminPlatform;
import org.example.event_platform.Entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final String SECRET_KEY = "bXktc3VwZXItc2VjcmV0LXN1cGVyLXNlY3JldC1rZXktMTIzNDU2Nzg5MA==";

    private final SecretKey key =
            Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    @Autowired
    private CustomUserDetailsService userDetailsService;

    // ---- Tạo token ----
    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(1, ChronoUnit.DAYS);

        // 1. Lấy shopId an toàn (Null-safe)
        Long shopId = (user.getTenant() != null) ? user.getTenant().getId() : null;

        // 2. Gom tất cả quyền từ Role và Permissions (đã viết ở getAuthorities)
        List<String> authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                // Nếu không có tenantId thì mặc định là PLATFORM, ngược lại là TENANT
                .claim("userType", (shopId == null) ? "PLATFORM" : "TENANT")
                .claim("shopId", shopId)
                .claim("authorities", authorities)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    // ---- Lấy Authentication từ token ----
    public Authentication getAuthentication(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String username = claims.getSubject();
            String userType = claims.get("userType", String.class);

            // QUAN TRỌNG: Load UserDetails (chính là Entity User của ông)
            // Thay vì dùng String username, ta dùng đối tượng User thật
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        } catch (Exception e) {
            return null;
        }
    }

    // ---- Lấy username từ token ----
    public String getUsernameFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    // ---- Lấy roleCode từ token ----
    public String getRoleCodeFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("roleCode", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    // ---- Lấy userId từ token (optional) ----
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            return null;
        }
    }

    // ---- Validate token ----
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    public Long getShopIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Object shopIdObj = claims.get("shopId"); // tên claim bạn lưu khi login
            if (shopIdObj instanceof Integer) {
                return ((Integer) shopIdObj).longValue();
            } else if (shopIdObj instanceof Long) {
                return (Long) shopIdObj;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    public String getUserTypeFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("userType", String.class);
        } catch (Exception e) {
            return null;
        }
    }
}
