package org.example.event_platform.Dto.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {
    @NotBlank(message = "Username không được để trống")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    @Email(message = "Email không đúng định dạng")
    private String email;

    private String phone;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    private Integer seniority;

    @NotNull(message = "Vui lòng gán vai trò cho người dùng")
    private Long roleId; // Admin chọn Role từ danh sách có sẵn
}