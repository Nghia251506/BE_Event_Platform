package org.example.event_platform.Dto.Event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignMemberRequest {
    private Long userId;     // ID của thành viên được gán
    private String position; // Vị trí: DAU_LAN, DUOI_LAN, TRONG, DIA...
}