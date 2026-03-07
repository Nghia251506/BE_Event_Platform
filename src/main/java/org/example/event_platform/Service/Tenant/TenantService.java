package org.example.event_platform.Service.Tenant;

import org.example.event_platform.Dto.Tenant.TenantRequest;
import org.example.event_platform.Dto.Tenant.TenantResponse;
import java.util.List;

public interface TenantService {
    // Đăng ký mới (Create)
    TenantResponse registerTenant(TenantRequest request);

    // Lấy thông tin (Read)
    TenantResponse getTenantById(Long id);
    List<TenantResponse> getAllTenants();

    // Cập nhật (Update)
    TenantResponse updateTenant(Long id, TenantRequest request);

    // Xóa hoặc Disable (Delete)
    void deleteTenant(Long id);

    // Xác thực email
    void verifyTenant(String token);
}