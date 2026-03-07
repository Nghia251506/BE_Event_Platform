package org.example.event_platform.Dto.Event;

import jakarta.validation.constraints.*;
import lombok.*;
import org.example.event_platform.Entity.EventType;

import java.math.BigDecimal;
import java.time.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    private String province;

    @NotBlank(message = "Tên khách hàng không được để trống")
    private String customerName;
    private String customerPhone;

    @NotNull(message = "Phải chọn đội lân thực hiện")
    private Long tenantId; // Admin sàn chọn đội lân nào sẽ diễn

    @DecimalMin(value = "0.0", message = "Số tiền không hợp lệ")
    private BigDecimal totalAmount;
    private BigDecimal platformFee;

    private String description;
}
