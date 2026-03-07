package org.example.event_platform.Dto.Dashboard;

import lombok.Data;

@Data
public class MemberStatusDTO {
    private String fullName;
    private String roleName;
    private String status; // active, busy
    private String avatar;
}
