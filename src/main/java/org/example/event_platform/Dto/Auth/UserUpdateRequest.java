package org.example.event_platform.Dto.Auth;

import lombok.*;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {
    private String email;
    private String phone;
    private String fullName;
    private Integer seniority;
    private String status; // ACTIVE, INACTIVE, vv.
    private Boolean isActive;
    private Long roleId;
    private Set<Long> permissionIds; // Admin có thể gán thêm quyền trực tiếp
}