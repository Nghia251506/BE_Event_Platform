package org.example.event_platform.Entity;

public enum EventStatus {
    SCHEDULED,    // Đã lên lịch
    ACCEPTED,
    REJECTED,
    IN_PROGRESS,  // Đang diễn ra
    COMPLETED,    // Hoàn thành
    CANCELLED     // Đã hủy
}
