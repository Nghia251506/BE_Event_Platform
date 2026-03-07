package org.example.event_platform.Entity;

public enum RegistrationStatus {
    PENDING_VERIFICATION, // Chờ bấm link email
    ACTIVE,               // Đã xác thực và hoạt động
    SUSPENDED             // Bị khóa
}
