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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private Long id;
    private String name;
    private EventType type;
    private String typeDisplayName; // Để hiện "Tết Trung Thu" thay vì "MID_AUTUMN"

    private EventStatus status;
    private String statusDisplayName; // Để hiện "Hoàn thành" thay vì "COMPLETED"

    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private String location;
    private String customerName;
    private String customerPhone;

    // Thông tin Tenant (Đội lân)
    private Long tenantId;
    private String tenantName;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime concentrateTime;

    private String concentrateLocation;
    // Tài chính
    private BigDecimal totalAmount;
    private BigDecimal platformFee; // Phí sàn thu

    private LocalDateTime createdAt;
}
