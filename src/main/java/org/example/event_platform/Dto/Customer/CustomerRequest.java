package org.example.event_platform.Dto.Customer;

import lombok.Data;

@Data
public class CustomerRequest {
    private String fullName;
    private String phone;
    private Long userId; // Admin chọn thành viên nào quản lý khách này
    private Long tenantId;
}
