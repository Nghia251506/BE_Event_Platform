package org.example.event_platform.Mapper;

import org.example.event_platform.Dto.Event.EventRequest;
import org.example.event_platform.Dto.Event.EventResponse;
import org.example.event_platform.Dto.Event.UpdateConcentrate;
import org.example.event_platform.Entity.Event;
import org.example.event_platform.Entity.Tenant;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventMapper {

    // 1. Từ Request (FE gửi lên) sang Entity (để lưu DB)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", source = "tenantId", qualifiedByName = "idToTenant")
    @Mapping(target = "platformFee", ignore = true) // Sẽ tính toán trong Service
    Event toEntity(EventRequest request);

    // 2. Từ Entity (DB) sang Response (để hiện lên FE)
    @Mapping(target = "tenantId", source = "tenant.id")
    @Mapping(target = "tenantName", source = "tenant.name")
    @Mapping(target = "typeDisplayName", source = "type.displayName")
    @Mapping(target = "statusDisplayName", expression = "java(formatStatus(event.getStatus()))")
    @Mapping(target = "customerPhone", source = "customerPhone")
    @Mapping(target = "concentrateTime", source = "concentrateTime")
    @Mapping(target = "concentrateLocation", source = "concentrateLocation")
    EventResponse toResponse(Event event);

    void updateConcentrate(UpdateConcentrate updateConcentrate, @MappingTarget Event event);

    // 3. Update Entity từ Request (Dùng cho sửa sự kiện)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", source = "tenantId", qualifiedByName = "idToTenant")
    void updateEventFromRequest(EventRequest request, @MappingTarget Event event);

    // Helper: Chuyển Long ID sang object Tenant (MapStruct sẽ tự hiểu để link)
    @Named("idToTenant")
    default Tenant idToTenant(Long tenantId) {
        if (tenantId == null) return null;
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        return tenant;
    }

    // Helper: Format status sang tiếng Việt (Ông có thể làm tương tự Enum Type)
    default String formatStatus(org.example.event_platform.Entity.EventStatus status) {
        if (status == null) return "";
        return switch (status) {
            case SCHEDULED -> "Đã lên lịch";
            case IN_PROGRESS -> "Đang diễn ra";
            case COMPLETED -> "Hoàn thành";
            case CANCELLED -> "Đã hủy";
            default -> status.name();
        };
    }
}