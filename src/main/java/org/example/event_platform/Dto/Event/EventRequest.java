package org.example.event_platform.Dto.Event;

import jakarta.validation.constraints.*;
import lombok.*;
import org.example.event_platform.Entity.EventType;

import java.math.BigDecimal;
import java.time.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EventRequest {
    @NotBlank(message = "Tên sự kiện không được để trống")
    private String name;

    private EventType type;

    @NotNull(message = "Ngày diễn ra không được để trống")
    private LocalDate eventDate;

    private LocalTime startTime;
    private LocalTime endTime;

    @NotBlank(message = "Địa điểm không được để trống")
    private String location;

    // PHẦN CUSTOMER: Ưu tiên ID, không có mới dùng String
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private Long tenantId;

    @DecimalMin(value = "0.0")
    private BigDecimal totalAmount;

    private String description;

    // Thêm thông tin tập trung ngay từ lúc tạo nếu có
    private LocalTime concentrateTime;
    private String concentrateLocation;
}
