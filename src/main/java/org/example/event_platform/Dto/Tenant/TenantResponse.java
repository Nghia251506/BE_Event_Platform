package org.example.event_platform.Dto.Tenant;

import org.example.event_platform.Entity.RegistrationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse {

    private Long id;
    private String name;
    private String domain;
    private String email;
    private String logo;
    private boolean active;

    private Boolean isVerified;
    private RegistrationStatus statusConfirm;

    // Thêm trường này để Frontend hiển thị ngày tham gia cho đẹp
    // LocalDateTime này MapStruct sẽ tự xử lý từ Entity qua
}
