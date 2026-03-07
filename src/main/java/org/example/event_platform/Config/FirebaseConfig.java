package org.example.event_platform.Config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @Value("${JSON_FCM:}") // Lấy từ biến môi trường JSON_FCM, mặc định là rỗng
    private String fcmJson;

    @Bean
    public FirebaseApp initializeFirebase() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        InputStream serviceAccount;

        // KIỂM TRA 1: Nếu có biến môi trường JSON_FCM (Dùng trên Google Cloud)
        if (fcmJson != null && !fcmJson.trim().isEmpty()) {
            serviceAccount = new ByteArrayInputStream(fcmJson.getBytes(StandardCharsets.UTF_8));
            System.out.println("Firebase: Đang khởi tạo bằng biến môi trường JSON_FCM");
        } 
        // KIỂM TRA 2: Nếu không có biến môi trường, tìm file vật lý (Dùng ở máy Local)
        else {
            ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
            if (resource.exists()) {
                serviceAccount = resource.getInputStream();
                System.out.println("Firebase: Đang khởi tạo bằng file vật lý trong resources");
            } else {
                // Nếu cả 2 đều không có thì báo lỗi rõ ràng
                throw new IllegalStateException("LỖI: Không tìm thấy biến JSON_FCM lẫn file firebase-service-account.json!");
            }
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        return FirebaseApp.initializeApp(options);
    }
}