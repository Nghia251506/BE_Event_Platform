package org.example.event_platform.Entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.*;

@Entity
@Table(name = "user_event")
@Data
public class UserEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    // Vị trí diễn: DAU_LAN, DUOI_LAN, ONG_DIA, TRONG, HA_QUAN...
    @Column(nullable = false)
    private String position;

    // Trạng thái xác nhận show: PENDING, ACCEPTED, REJECTED
    @Column(nullable = false)
    private AssignStatus status = AssignStatus.PENDING;

    // Lý do nếu thành viên từ chối (REJECTED)
    private String note;

    // Thời gian thành viên bấm xác nhận/từ chối
    private LocalDateTime respondedAt;

    // --- Phần Điểm Danh ---

    // Thời gian Member bấm check-in tại điểm diễn
    private LocalTime checkinAt;

    // Thời gian Member bấm check-out sau khi diễn xong
    private LocalTime checkoutAt;

    // Tọa độ lúc check-in (nếu ông muốn làm gắt vụ đứng đúng chỗ mới được điểm danh)
    private String checkinLocation;

    private LocalTime concentrateAt;
    private String concentrateLocationActual;
}
