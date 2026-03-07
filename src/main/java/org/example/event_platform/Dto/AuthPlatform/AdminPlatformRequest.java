package org.example.event_platform.Dto.AuthPlatform;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPlatformRequest {
    @NotBlank(message = "Username không được để trống")
    private String username;

    @NotBlank(message = "Password không được để trống")
    private String password;

    @Email(message = "Email không hợp lệ")
    private String email;

    private String phone;
    private String fullName;
    private Integer seniority;

    private Long roleId; // ID của vai trò (Sales, Support, Admin)
    private Set<Long> permissionIds; // Danh sách ID các quyền gán thêm
}
