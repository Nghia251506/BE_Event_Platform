package org.example.event_platform.Mapper;

import org.example.event_platform.Dto.Tenant.TenantRequest;
import org.example.event_platform.Dto.Tenant.TenantResponse;
import org.example.event_platform.Entity.Tenant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TenantMapper {

    // Chuyển từ Entity sang Response DTO (Dùng để trả về API)
    TenantResponse toResponse(Tenant tenant);

    // Chuyển từ Request DTO sang Entity (Dùng cho đăng ký mới)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "verificationToken", ignore = true)
    @Mapping(target = "verificationTokenExpiry", ignore = true)
    @Mapping(target = "isVerified", ignore = true)
    @Mapping(target = "statusConfirm", ignore = true)
    Tenant toEntity(TenantRequest request);

    // Cập nhật Entity từ Request (Dùng cho tính năng Update)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "verificationToken", ignore = true)
    @Mapping(target = "verificationTokenExpiry", ignore = true)
    @Mapping(target = "isVerified", ignore = true)
    @Mapping(target = "statusConfirm", ignore = true)
    void updateTenantFromRequest(TenantRequest request, @MappingTarget Tenant tenant);
}