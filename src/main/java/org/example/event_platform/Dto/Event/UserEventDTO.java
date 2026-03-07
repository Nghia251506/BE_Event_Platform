package org.example.event_platform.Dto.Event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class UserEventDTO {
    private Long id;
    private Long eventId;
    private String eventName;
    private String eventDate;
    private String location;

    private Long userId;
    private String fullName;
    private String position;
    private String status; // PENDING, ACCEPTED, REJECTED, FINISHED

    private LocalTime checkinAt;
    private LocalTime checkoutAt;

    private LocalTime startTime;
    private LocalTime endTime;

    private LocalTime concentrateTime;
    private String concentrateLocation;
    private String note;

    // Chỉ dùng list này khi vào trang "Chi tiết lịch diễn của tôi"
    private List<TeammateDTO> teammates;

    @Data @AllArgsConstructor
    public static class TeammateDTO {
        private String fullName;
        private String position;
        private String status;
    }
}
