package org.example.event_platform.Dto.Auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.example.event_platform.Entity.UserEvent;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String fullName;
    private Integer seniority;
    private String status;
    private Boolean isVerified;
    private Boolean isActive;

    // Thông tin Tenant (Để FE biết user này thuộc đội nào)
    private Long tenantId;
    private String tenantName;

    // Thông tin Role
    private String roleName;

    // Danh sách các quyền (Permissions)
    private Set<String> permissions;
    @JsonIgnore
    private List<UserEvent> userEvents;
}