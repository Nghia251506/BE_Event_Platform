package org.example.event_platform.Service.FCM;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FCMService {

    public void sendPushNotification(String token, String title, String body, Map<String, String> data) {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data) // Gửi kèm ID phiên để FE fetch dữ liệu
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Gửi thông báo thành công: " + response);
        } catch (FirebaseMessagingException e) {
            System.err.println("Lỗi gửi thông báo: " + e.getMessage());
        }
    }

    // HÀM MỚI: Gửi Silent Data Message - Chuyên dùng cho In-app Popup kiểu Grab
    public void sendDataMessage(String token, Map<String, String> data) {
        Message message = Message.builder()
                .setToken(token)
                .putAllData(data) // Chỉ gửi data, không gửi Notification object
                .build();
        sendMessage(message);
    }

    private void sendMessage(Message message) {
        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Sent successfully: " + response);
        } catch (FirebaseMessagingException e) {
            System.err.println("Error sending: " + e.getMessage());
        }
    }
}
