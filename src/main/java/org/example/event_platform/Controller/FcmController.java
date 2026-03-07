package org.example.event_platform.Controller;


import org.example.event_platform.Service.FCM.FcmTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/fcm")
public class FcmController {

    @Autowired
    private FcmTokenService fcmTokenService;

    @PostMapping("/register")
    public ResponseEntity<?> registerToken(@RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        String token = payload.get("token").toString();

        fcmTokenService.saveToken(userId, token);
        return ResponseEntity.ok("Đăng ký Token thành công!");
    }

    // @PostMapping("/test-push")
    // public ResponseEntity<?> testPush(@RequestParam String token) {
    //     fcmService.sendPushNotification(
    //             token,
    //             "Test Swagger",
    //             "Nếu bạn thấy cái này thì cấu hình BE đã CHUẨN!",
    //             Map.of("type", "TEST"));
    //     return ResponseEntity.ok("Đã gửi lệnh push!");
    // }
}
