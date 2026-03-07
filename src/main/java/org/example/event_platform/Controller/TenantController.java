package org.example.event_platform.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.event_platform.Dto.Tenant.TenantRequest;
import org.example.event_platform.Dto.Tenant.TenantResponse;
import org.example.event_platform.Service.Tenant.TenantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    // 1. API Đăng ký gian hàng mới (Public)
    @PostMapping("/public/register")
    public ResponseEntity<TenantResponse> register(@Valid @RequestBody TenantRequest request) {
        TenantResponse response = tenantService.registerTenant(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 2. API Xác thực Email khi khách click vào Link (Public)
    // URL: /api/tenants/public/verify?token=abc-xyz
    @GetMapping("/public/verify")
    public ResponseEntity<String> verify(@RequestParam("token") String token) {
        tenantService.verifyTenant(token);
        return ResponseEntity.ok("Xác thực thành công! Mật khẩu đăng nhập đã được gửi vào Email của bạn.");
    }

    // 3. Lấy danh sách tất cả gian hàng (Admin mới dùng)
    @GetMapping
//    @PreAuthorize("hasAuthority('VIEW_ALL_TENANT')")
    public ResponseEntity<List<TenantResponse>> getAll() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }

    // 4. Lấy chi tiết một gian hàng
    @GetMapping("/{id}")
//    @PreAuthorize("hasAuthority('VIEW_TENANT_DETAIL')")
    public ResponseEntity<TenantResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tenantService.getTenantById(id));
    }

    // 5. Cập nhật thông tin gian hàng
    @PutMapping("/{id}")
//    @PreAuthorize("hasAuthority('UPDATE_TENANT')")
    public ResponseEntity<TenantResponse> update(@PathVariable Long id, @RequestBody TenantRequest request) {
        return ResponseEntity.ok(tenantService.updateTenant(id, request));
    }

    // 6. Xóa/Ngừng kích hoạt gian hàng
    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAuthority('DELETE_TENANT')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tenantService.deleteTenant(id);
        return ResponseEntity.noContent().build();
    }
}