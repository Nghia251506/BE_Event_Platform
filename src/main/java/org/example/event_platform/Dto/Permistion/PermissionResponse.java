package org.example.event_platform.Dto.Permistion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionResponse {

    private Long id;
    private String name;
    private String description;
    private Long tenantId;

    // Helper method để FE dễ check nhanh
    public boolean isGlobal() {
        return this.tenantId == null;
    }
}