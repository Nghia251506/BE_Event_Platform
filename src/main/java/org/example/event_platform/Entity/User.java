package org.example.event_platform.Entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;
    private String password;
    private String email;
    private String phone;
    private String fullName;
    private Integer Seniority;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private UserStatus status = UserStatus.ACTIVE;
    @Column(name = "verification_token")
    private String verificationToken;
    private Boolean isActive = false;

    @Column(name = "verification_token_expiry")
    private LocalDateTime verificationTokenExpiry;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_confirm", length = 30)
    private RegistrationStatus statusConfirm = RegistrationStatus.PENDING_VERIFICATION;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role roles;

    // Quyền được gán trực tiếp (Đây chính là bảng user_permissions bạn nhắc tới)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_permissions",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;

    @OneToMany(mappedBy = "user",fetch = FetchType.EAGER)
    @JsonIgnore // Tránh bị vòng lặp vô tận khi render JSON
    private List<UserEvent> userEvents = new ArrayList<>();

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Dùng Set để tự động loại bỏ các quyền trùng lặp
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        // 1. Lấy quyền từ Role (role_permissions)
        if (this.roles != null && this.roles.getPermissions() != null) {
            this.roles.getPermissions().forEach(p ->
                    authorities.add(new SimpleGrantedAuthority(p.getName()))
            );
            // Thêm chính cái Role đó với prefix ROLE_ (Dùng cho hasRole trong SecurityConfig)
            authorities.add(new SimpleGrantedAuthority("ROLE_" + this.roles.getName()));
        }

        // 2. Lấy thêm quyền đặc cách được gán trực tiếp (user_permissions)
        if (this.permissions != null) {
            this.permissions.forEach(p ->
                    authorities.add(new SimpleGrantedAuthority(p.getName()))
            );
        }

        return authorities;
    }

    @Override
    @JsonIgnore
    public String getPassword() { return this.password; }

    // Các hàm này trả về true và không liên quan tới DB
    @Override
    @Transient // Báo cho Hibernate: "Đừng có tìm cột này trong DB"
    @JsonIgnore
    public boolean isAccountNonExpired() { return true; }

    @Override
    @Transient
    @JsonIgnore
    public boolean isAccountNonLocked() { return true; }

    @Override
    @Transient
    @JsonIgnore
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    @Transient
    @JsonIgnore
    public boolean isEnabled() { return true; }
}
