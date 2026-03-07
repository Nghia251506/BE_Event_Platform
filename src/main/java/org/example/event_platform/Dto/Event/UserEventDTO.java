package org.example.event_platform.Dto.Event;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class UserEventDTO {
    private Long id;
    private Long eventId;
    private Long userId;
    private String fullName; // Thêm tên để Flutter hiển thị luôn, khỏi query thêm
    private String position;
    private String status;
    private String note;
    private LocalTime checkinAt;
    private LocalTime checkoutAt;
    private String eventName;
    private String eventDate;
    private String location;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalTime concentrateTime;
    private String concentrateLocation;
    private List<TeammateDTO> teammates;

    @Data
    public static class TeammateDTO {
        private String fullName;
        private String position;
        private String status;
    }
}
