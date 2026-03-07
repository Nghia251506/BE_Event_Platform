package org.example.event_platform.Dto.Event;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.event_platform.Entity.EventStatus;
import org.example.event_platform.Entity.EventType;
import org.example.event_platform.Entity.UserEvent;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EventResponse {
    private Long id;
    private String name;
    private EventType type;
    private String typeDisplayName;

    private EventStatus status;
    private String statusDisplayName;

    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private String location;

    // Lấy từ Object Customer sang, không dùng field String ở Event nữa
    private String customerName;
    private String customerPhone;

    private Long tenantId;
    private String tenantName;

    private LocalTime concentrateTime;
    private String concentrateLocation;

    private BigDecimal totalAmount;
    private BigDecimal platformFee;

    private LocalDateTime createdAt;
}
