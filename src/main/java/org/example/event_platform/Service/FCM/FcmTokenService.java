package org.example.event_platform.Service.FCM;

import jakarta.transaction.Transactional;
import org.example.event_platform.Entity.FcmToken;
import org.example.event_platform.Entity.User;
import org.example.event_platform.Repository.FcmTokenRepository;
import org.example.event_platform.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class FcmTokenService {
    @Autowired
    private FcmTokenRepository tokenRepository;
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void saveToken(Long userId, String token) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Nếu token đã tồn tại thì cập nhật thời gian, nếu chưa thì tạo mới
        List<FcmToken> fcmTokens = tokenRepository.findByToken(token);
        FcmToken fcmToken = fcmTokens.isEmpty() ? new FcmToken() : fcmTokens.get(0);

        fcmToken.setToken(token);
        fcmToken.setUser(user);
        fcmToken.setLastUpdated(Instant.now());

        tokenRepository.save(fcmToken);
    }

    // Thêm vào FcmTokenService.java
    public String getTokenByUserId(Long userId) {
        List<FcmToken> tokens = tokenRepository.findByUserId(userId);

        if (tokens.isEmpty()) {
            return null;
        }

        // Lấy token của thiết bị cập nhật gần đây nhất
        return tokens.get(tokens.size() - 1).getToken();
    }
}
