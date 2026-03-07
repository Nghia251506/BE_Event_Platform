package org.example.event_platform.Service;

import org.example.event_platform.Config.MailConfig;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final MailConfig mailSender;

    public void sendVerificationEmail(String to, String token, Long shopId) {
        String verificationUrl = "http://localhost:8080/api/tenants/public/verify?token=" + token + "&shopId=" + shopId;

        String subject = "[EVENT POS] Xác thực email để kích hoạt gian hàng";

        String content = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #eee; border-radius: 10px;">
                <h2 style="color: #1e40af;">Chào mừng bạn đến với EVENT POS!</h2>
                <p>Cảm ơn bạn đã đăng ký gian hàng. Vui lòng click nút dưới đây để xác thực email và kích hoạt gian hàng:</p>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="%s" style="background: #1e40af; color: white; padding: 14px 28px; text-decoration: none; border-radius: 8px; font-weight: bold;">Xác thực email ngay</a>
                </div>
                <p>Link sẽ hết hạn sau 24 giờ.</p>
                <p>Nếu bạn không đăng ký, vui lòng bỏ qua email này.</p>
                <hr>
                <p style="color: #666; font-size: 12px;">Trân trọng,<br>Đội ngũ EVENT POS</p>
            </div>
            """.formatted(verificationUrl);

        sendHtmlEmail(to, subject, content);
    }

    public void sendCredentialsEmail(String to, String username, String password) {
        String subject = "[EVENT POS] Tài khoản quản trị của bạn";

        String content = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #eee; border-radius: 10px;">
                <h2 style="color: #16a34a;">Gian hàng của bạn đã được kích hoạt thành công!</h2>
                <p>Bạn có thể đăng nhập bằng thông tin sau:</p>
                <div style="background: #f3f4f6; padding: 16px; border-radius: 8px; margin: 20px 0;">
                    <p><strong>Tên đăng nhập:</strong> %s</p>
                    <p><strong>Mật khẩu tạm thời:</strong> %s</p>
                </div>
                <p style="color: #dc2626; font-weight: bold;">Vui lòng đổi mật khẩu ngay sau khi đăng nhập lần đầu!</p>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="http://localhost:3000/check-shop" style="background: #16a34a; color: white; padding: 14px 28px; text-decoration: none; border-radius: 8px; font-weight: bold;">Đăng nhập ngay</a>
                </div>
                <hr>
                <p style="color: #666; font-size: 12px;">Trân trọng,<br>Đội ngũ EVENT POS</p>
            </div>
            """.formatted(username, password);

        sendHtmlEmail(to, subject, content);
    }

    private void sendHtmlEmail(String to, String subject, String content) {
        MimeMessage message = mailSender.javaMailSender().createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true = HTML
            mailSender.javaMailSender().send(message);
        } catch (Exception e) {
            throw new RuntimeException("Gửi email thất bại: " + e.getMessage());
        }
    }
}
