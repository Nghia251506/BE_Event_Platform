package org.example.event_platform.Entity;

public enum EventStatus {
    SCHEDULED,    // Đã lên lịch
    CONFIRMED,
    IN_PROGRESS,  // Đang diễn ra
    COMPLETED,    // Hoàn thành
    CANCELLED
}
