package org.example.event_platform.Entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity
@Table(name = "user_event")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserEvent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Chuyển sang LAZY để cứu Server
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String position; // DAU_LAN, TRONG, DIA...

    @Enumerated(EnumType.STRING)
    private AssignStatus status;
    // Vòng đời: PENDING -> ACCEPTED/REJECTED -> CHECKED_IN -> CHECKED_OUT (FINISHED)

    private String note; // Lý do từ chối
    private LocalDateTime respondedAt;

    // PHỤC VỤ CHECK-IN/OUT VÀ AUTO-CLOSE SHOW
    private LocalTime checkinAt;
    private LocalTime checkoutAt;
    private String checkinLocation; // Tọa độ GPS thực tế

    // Thông tin tập trung thực tế
    private LocalTime actualConcentrateAt;
}
