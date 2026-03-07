package org.example.event_platform.Service.Customer;

import lombok.RequiredArgsConstructor;
import org.example.event_platform.Dto.Customer.CustomerRequest;
import org.example.event_platform.Dto.Customer.CustomerResponse;
import org.example.event_platform.Entity.Customer;
import org.example.event_platform.Entity.User;
import org.example.event_platform.Mapper.CustomerMapper;
import org.example.event_platform.Repository.CustomerRepository;
import org.example.event_platform.Repository.UserRepository;
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

    /**
     * TẠO MỚI: Tự động lấy User đang login để gán vào AssignedTo
     */
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        // 1. Lấy Username từ JWT/Security Context
        String currentUsername = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        // 2. Tìm User thực thể
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Lỗi hệ thống: Không tìm thấy User đang đăng nhập!"));

        // 3. Check trùng số điện thoại
        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Số điện thoại này đã thuộc về một khách hàng khác!");
        }

        // 4. Map & Gán người quản lý
        Customer customer = customerMapper.toEntity(request);
        customer.setAssignedTo(currentUser); // Tự động "đóng dấu"

        return customerMapper.toResponse(customerRepository.save(customer));
    }

    /**
     * SEARCH & LIST: Tích hợp phân trang và tìm kiếm theo Keyword (Tên/SĐT)
     */
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getCustomers(String keyword, Pageable pageable) {
        return customerRepository.searchCustomers(keyword, pageable)
                .map(customerMapper::toResponse);
    }

    /**
     * GET BY ID: Lấy chi tiết khách hàng
     */
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với ID: " + id));
        return customerMapper.toResponse(customer);
    }

    /**
     * UPDATE: Cập nhật thông tin và check trùng Phone
     */
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại để cập nhật!"));

        // Nếu đổi số điện thoại, phải check xem số mới có ai dùng chưa
        if (!existingCustomer.getPhone().equals(request.getPhone()) &&
                customerRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Số điện thoại mới đã bị trùng!");
        }

        customerMapper.updateEntityFromRequest(request, existingCustomer);
        return customerMapper.toResponse(customerRepository.save(existingCustomer));
    }

    /**
     * DELETE: Xóa khách hàng khỏi hệ thống
     */
    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("ID khách hàng không hợp lệ!");
        }
        customerRepository.deleteById(id);
    }
}
