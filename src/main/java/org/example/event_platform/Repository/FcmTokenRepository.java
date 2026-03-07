package org.example.event_platform.Repository;

import org.example.event_platform.Entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    // Tìm tất cả token của 1 user (để gửi thông báo đến mọi thiết bị họ đang dùng)
    List<FcmToken> findByUserId(Long userId);
    
    // Tìm token cụ thể để tránh lưu trùng lặp
    List<FcmToken> findByToken(String token);
}
