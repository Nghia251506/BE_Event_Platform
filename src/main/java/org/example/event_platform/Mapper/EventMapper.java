package org.example.event_platform.Mapper;

import org.example.event_platform.Dto.Event.EventRequest;
import org.example.event_platform.Dto.Event.EventResponse;
import org.example.event_platform.Dto.Event.UpdateConcentrate;
import org.example.event_platform.Entity.Customer;
import org.example.event_platform.Entity.Event;
import org.example.event_platform.Entity.Tenant;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventMapper {

    // 1. Từ Request sang Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", source = "tenantId", qualifiedByName = "idToTenant")
    @Mapping(target = "customer", source = "customerId", qualifiedByName = "idToCustomer")
    @Mapping(target = "platformFee", ignore = true)
    Event toEntity(EventRequest request);

    // 2. Từ Entity sang Response (Lấy data từ Object liên kết)
    @Mapping(target = "tenantId", source = "tenant.id")
    @Mapping(target = "tenantName", source = "tenant.name")
    @Mapping(target = "typeDisplayName", source = "type.displayName")
    @Mapping(target = "statusDisplayName", expression = "java(formatEventStatus(event.getStatus()))")
    // LẤY TRỰC TIẾP TỪ CUSTOMER OBJECT (Bỏ field String ở Event)
    @Mapping(target = "customerName", source = "customer.fullName")
    @Mapping(target = "customerPhone", source = "customer.phone")
    EventResponse toResponse(Event event);

    void updateConcentrate(UpdateConcentrate updateConcentrate, @MappingTarget Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", source = "tenantId", qualifiedByName = "idToTenant")
    @Mapping(target = "customer", source = "customerId", qualifiedByName = "idToCustomer")
    void updateEventFromRequest(EventRequest request, @MappingTarget Event event);

    @Named("idToTenant")
    default Tenant idToTenant(Long tenantId) {
        if (tenantId == null) return null;
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        return tenant;
    }

    @Named("idToCustomer")
    default Customer idToCustomer(Long customerId) {
        if (customerId == null) return null;
        Customer customer = new Customer();
        customer.setId(customerId);
        return customer;
    }

    // Helper: Tiếng Việt hóa trạng thái Show
    default String formatEventStatus(org.example.event_platform.Entity.EventStatus status) {
        if (status == null) return "";
        return switch (status) {
            case SCHEDULED -> "Chờ duyệt";     // Mới tạo hoặc đang chờ ghép đội
            case CONFIRMED -> "Đã chốt show";  // Đã đủ người, sẵn sàng diễn
            case IN_PROGRESS -> "Đang diễn";   // Có người đã check-in
            case COMPLETED -> "Hoàn thành";   // Tất cả đã checkout
            case CANCELLED -> "Đã hủy";
        };
    }
}