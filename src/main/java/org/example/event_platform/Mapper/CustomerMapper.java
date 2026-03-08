package org.example.event_platform.Mapper;

import org.example.event_platform.Dto.Customer.CustomerRequest;
import org.example.event_platform.Dto.Customer.CustomerResponse;
import org.example.event_platform.Entity.Customer;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    // 1. Chuyển Entity sang Response để trả về UI
    @Mapping(source = "assignedTo.id", target = "assignedToId")
    @Mapping(source = "assignedTo.fullName", target = "assignedToName")
    CustomerResponse toResponse(Customer customer);

    // 2. Chuyển Request sang Entity để tạo mới
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    Customer toEntity(CustomerRequest request);

    // 3. CẬP NHẬT: Đây là hàm ông giáo đang thiếu
    // @MappingTarget giúp MapStruct hiểu là: "Lấy data từ request đè vào existingCustomer"
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromRequest(CustomerRequest request, @MappingTarget Customer existingCustomer);

    // 4. Logic xử lý null (Optional): Nếu field nào trong request null thì không đè lên cái cũ
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityWithIgnoreNull(CustomerRequest request, @MappingTarget Customer existingCustomer);
}
