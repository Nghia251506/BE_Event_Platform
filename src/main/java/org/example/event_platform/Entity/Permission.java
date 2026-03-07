package org.example.event_platform.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // Ví dụ: "CREATE_BOOKING", "DELETE_ARTICLE", "VIEW_REPORTS"

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "roles_ibfk_1"))
    private Tenant tenant;
}
