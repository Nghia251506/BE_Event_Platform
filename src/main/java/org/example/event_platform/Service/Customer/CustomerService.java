package org.example.event_platform.Service.Customer;

import lombok.RequiredArgsConstructor;
import org.example.event_platform.Dto.Customer.CustomerRequest;
import org.example.event_platform.Dto.Customer.CustomerResponse;
import org.example.event_platform.Entity.Customer;
import org.example.event_platform.Entity.User;
import org.example.event_platform.Mapper.CustomerMapper;
import org.example.event_platform.Repository.CustomerRepository;
import org.example.event_platform.Repository.UserRepository;
import org.example.event_platform.util.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final CustomerMapper customerMapper;
    // Giả sử TenantContext của ông có hàm getCurrentTenantId()

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        Long currentTenantId = TenantContext.getCurrentShopId();

        // 1. Check trùng SĐT trong nội bộ Tenant
        if (customerRepository.existsByPhoneAndTenantId(request.getPhone(), currentTenantId)) {
            throw new RuntimeException("Số điện thoại này đã tồn tại trong hệ thống của bạn!");
        }

        // 2. Lấy User đang login
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));

        // 3. Map Entity
        Customer customer = customerMapper.toEntity(request);

        // 4. Gán Tenant và Người quản lý
        customer.setTenant(currentUser.getTenant()); // Hoặc lấy từ DB nếu TenantContext chỉ có ID
        customer.setAssignedTo(currentUser);

        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> getCustomers(String keyword, Pageable pageable) {
        Long tenantId = TenantContext.getCurrentShopId();
        return customerRepository.searchCustomers(tenantId, keyword, pageable)
                .map(customerMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        Long tenantId = TenantContext.getCurrentShopId();
        // Phải tìm theo cả ID và TenantId để bảo mật
        Customer customer = customerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng!"));
        return customerMapper.toResponse(customer);
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Long tenantId = TenantContext.getCurrentShopId();
        Customer existingCustomer = customerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại!"));

        // Check trùng phone (trừ chính nó ra)
        if (!existingCustomer.getPhone().equals(request.getPhone()) &&
                customerRepository.existsByPhoneAndTenantId(request.getPhone(), tenantId)) {
            throw new RuntimeException("Số điện thoại mới đã bị trùng trong hệ thống!");
        }

        customerMapper.updateEntityFromRequest(request, existingCustomer);
        return customerMapper.toResponse(customerRepository.save(existingCustomer));
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Long tenantId = TenantContext.getCurrentShopId();
        // Kiểm tra xem khách có thuộc quyền quản lý của Tenant không trước khi xóa
        Customer customer = customerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Bạn không có quyền xóa khách hàng này!"));

        customerRepository.delete(customer);
    }
}
