package org.example.event_platform.Dto.Permistion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionRequest {

    @NotBlank(message = "Tên quyền không được để trống")
    @Size(min = 3, max = 50, message = "Tên quyền phải từ 3 đến 50 ký tự")
    private String name;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    // Cái này có thể null nếu là SUPER_ADMIN tạo quyền Global
    // Hoặc sẽ được Service tự điền nếu là ADMIN đội tạo
    private Long tenantId;
}