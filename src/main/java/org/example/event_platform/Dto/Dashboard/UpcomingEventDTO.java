package org.example.event_platform.Dto.Dashboard;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpcomingEventDTO {
    private Long id;
    private String name;
    private String clientName;
    private LocalDateTime startTime;
    private String location;
    private String status;
    private BigDecimal price;
}
