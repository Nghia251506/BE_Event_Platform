package org.example.event_platform.Dto.AuthPlatform;

import lombok.*;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPlatformResponse {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String fullName;
    private Integer seniority;
    private String status;
    private Boolean isActive;

    // Trả về tên Role
    private String roleName;

    // Trả về danh sách Permission keys (Vd: ["MANAGE_TENANT", "APPROVE_PAYMENT"])
    private Set<String> permissions;
}
