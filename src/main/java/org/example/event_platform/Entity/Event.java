package org.example.event_platform.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event extends BaseEntity { // Nếu ông có BaseEntity chứa createdBy, updatedBy thì kế thừa

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private EventType type; // Enum: CORPORATE, GROUNDBREAKING, PARTY, WEDDING...

    @Enumerated(EnumType.STRING)
    private EventStatus status; // Enum: SCHEDULED, COMPLETED, CANCELLED, IN_PROGRESS

    // Thời gian
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;

    // Địa điểm
    @Column(columnDefinition = "TEXT")
    private String location;
    private String province;

    // Thông tin khách hàng (Lưu text đơn giản hoặc link bảng Customer tùy ông)
    private String customerName;
    private String customerPhone;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // QUAN TRỌNG: Liên kết với Tenant (Đội lân)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    // Tài chính (Dùng BigDecimal cho tiền tệ là chuẩn nhất)
    private BigDecimal totalAmount;      // Tổng giá trị show
    private Double commissionRate;      // % phí sàn thu (ví dụ: 10.0)
    private BigDecimal platformFee;     // Số tiền sàn thu về

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.EAGER)
    private List<UserEvent> assignedMembers = new ArrayList<>();
    private LocalTime concentrateTime;
    private String concentrateLocation;
}
