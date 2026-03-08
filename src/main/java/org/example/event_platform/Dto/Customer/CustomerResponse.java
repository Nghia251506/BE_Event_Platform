package org.example.event_platform.Dto.Customer;

import lombok.Data;

@Data
public class CustomerResponse {
    private Long id;
    private String fullName;
    private String phone;
    private Long assignedToId;      // ID của thành viên quản lý
    private String assignedToName;  // Tên của thành viên quản lý (để hiển thị luôn trên UI)
    private Long tenantId;
}
