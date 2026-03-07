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
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Event extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private EventType type;

    @Enumerated(EnumType.STRING)
    private EventStatus status; // SCHEDULED -> CONFIRMED -> IN_PROGRESS -> COMPLETED

    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;

    @Column(columnDefinition = "TEXT")
    private String location;

    // CHỈ DÙNG OBJECT - Bỏ customerName, customerPhone dư thừa
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    // Tài chính chuẩn chỉnh
    private BigDecimal totalAmount;
    private BigDecimal platformFee;

    // Thông tin tập trung (Để bắn NOTIFY cho anh em)
    private LocalTime concentrateTime;
    private String concentrateLocation;
    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserEvent> assignedMembers = new ArrayList<>();
}
