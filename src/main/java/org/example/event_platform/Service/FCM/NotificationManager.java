package org.example.event_platform.Service.FCM;

import lombok.extern.slf4j.Slf4j;
import org.example.event_platform.Entity.FcmToken;
import org.example.event_platform.Entity.User;
import org.example.event_platform.Repository.FcmTokenRepository;
import org.example.event_platform.Repository.UserRepository;
import org.example.event_platform.util.TenantContext; // Import cái util của ông vào
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class NotificationManager {
    @Autowired
    private FCMService fcmService;
    @Autowired
    private FcmTokenRepository tokenRepository;
    @Autowired
    private UserRepository userRepository;

    /**
     * Gửi thông báo cho TẤT CẢ ADMIN của đoàn (Tenant hiện tại)
     */
    public void notifyAdminsOfCurrentTenant(String type, String message, String sound) {
        Long tenantId = TenantContext.getCurrentShopId();

        if (tenantId == null) {
            log.warn("Không tìm thấy TenantId trong context - Có thể là Platform Admin hoặc lỗi Filter");
            return;
        }

        // Tìm dàn Admin của đoàn này
        // Lưu ý: Tên Role "ROLE_ADMIN" hoặc roleId 1 tùy ông giáo config trong DB nhé
        List<User> admins = userRepository.findByTenantIdAndRolesName(tenantId, "ROLE_ADMIN");

        Map<String, String> data = new HashMap<>();
        data.put("type", type);
        data.put("message", message);
        data.put("sound", sound != null ? sound : "notification.mp3");

        admins.forEach(admin -> sendDataToUser(admin.getId(), data));
        log.info("Đã gửi thông báo loại {} tới dàn Admin của Tenant: {}", type, tenantId);
    }

    /**
     * Gửi thông báo cho TOÀN BỘ ANH EM trong đoàn (Tenant hiện tại)
     */
        public void broadcastToCurrentTenant(String title, String content) {
            Long tenantId = TenantContext.getCurrentShopId();
            if (tenantId == null) return;

            List<User> allMembers = userRepository.findByTenantId(tenantId);

            Map<String, String> data = new HashMap<>();
            data.put("type", "TEAM_ANNOUNCEMENT");
            data.put("title", title);
            data.put("message", content);
            data.put("sound", "notification.mp3");

            allMembers.forEach(user -> sendDataToUser(user.getId(), data));
        }

    // --- 3. CÁC HÀM NGHIỆP VỤ CỤ THỂ ---

    // Member xác nhận -> Bắn cho dàn Admin của đoàn đó
    public void notifyAdminMemberAccepted(String memberName, String eventName) {
        String msg = "✅ [" + memberName + "] đã CHẤP NHẬN show: " + eventName;
        notifyAdminsOfCurrentTenant("MEMBER_ACCEPTED", msg, "success_ding.mp3");
    }

    // Member từ chối -> Bắn cho dàn Admin của đoàn đó báo động
    public void notifyAdminMemberRejected(String memberName, String eventName, String reason) {
        String msg = "⚠️ CẢNH BÁO: [" + memberName + "] TỪ CHỐI show " + eventName + ". Lý do: " + reason;
        notifyAdminsOfCurrentTenant("MEMBER_REJECTED", msg, "warning.mp3");
    }

    // Nhắc nhở tập trung (Dành cho Member cụ thể)
    public void notifyMemberConcentrate(Long memberId, String eventName, String time, String location) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "CONCENTRATE_REMINDER");
        data.put("message", "⏰ Nhắc nhở tập trung: Show " + eventName + " lúc " + time + " tại " + location);
        data.put("sound", "drum_roll.mp3");
        sendDataToUser(memberId, data);
    }

    // Gán show mới cho Member
    public void notifyMemberAssigned(Long memberId, String eventName, String position) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "NEW_EVENT_ASSIGNED");
        data.put("message", "🐲 Lịch diễn mới: " + eventName + " - Vị trí: " + position);
        data.put("sound", "notification.mp3");
        sendDataToUser(memberId, data);
    }

    /**
     * Gửi tin nhắn thực thi (Lower Level)
     */
    private void sendDataToUser(Long userId, Map<String, String> data) {
        List<FcmToken> tokens = tokenRepository.findByUserId(userId);
        if (tokens != null && !tokens.isEmpty()) {
            tokens.forEach(t -> {
                try {
                    fcmService.sendDataMessage(t.getToken(), data);
                } catch (Exception e) {
                    log.error("Lỗi gửi FCM cho Token {}: {}", t.getToken(), e.getMessage());
                }
            });
        }
    }
}