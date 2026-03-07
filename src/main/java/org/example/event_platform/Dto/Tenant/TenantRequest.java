package org.example.event_platform.Dto.Tenant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantRequest {

    @NotBlank(message = "Tên đơn vị không được để trống")
    @Size(max = 255, message = "Tên quá dài")
    private String name;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    private String domain; // Có thể để trống lúc đầu, admin cấp sau

    private String logo;

    private Boolean active; // Dùng khi Admin muốn disable một đội
}
