package org.example.event_platform.Dto.Tenant;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TenantSimpleResponse {
    private Long id;
    private String name;
}
