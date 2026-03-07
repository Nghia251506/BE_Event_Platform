package org.example.event_platform.Repository;

import org.example.event_platform.Entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    // Tìm theo domain (Quan trọng để xác định Tenant khi khách truy cập vào URL riêng)
    Optional<Tenant> findByDomain(String domain);

    // Tìm theo email (Dùng khi đăng ký để tránh trùng lặp)
    Optional<Tenant> findByEmail(String email);

    // Tìm theo token xác thực (Dùng cho luồng Click link từ Email)
    Optional<Tenant> findByVerificationToken(String token);

    // Kiểm tra nhanh xem email đã tồn tại chưa để báo lỗi sớm
    boolean existsByEmail(String email);

    // Kiểm tra domain đã bị ai đăng ký chưa
    boolean existsByDomain(String domain);
}