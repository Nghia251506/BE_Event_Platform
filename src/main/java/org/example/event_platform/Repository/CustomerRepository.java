package org.example.event_platform.Repository;

import org.example.event_platform.Entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // 1. Tìm kiếm theo Keyword + Tenant
    @Query("SELECT c FROM Customer c WHERE c.tenant.id = :tenantId AND " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "c.phone LIKE CONCAT('%', :keyword, '%'))")
    Page<Customer> searchCustomers(@Param("tenantId") Long tenantId,
                                   @Param("keyword") String keyword,
                                   Pageable pageable);

    // 2. Tìm khách của chính mình + Tenant
    @Query("SELECT c FROM Customer c WHERE c.tenant.id = :tenantId AND " +
            "c.assignedTo.id = :userId AND " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "c.phone LIKE CONCAT('%', :keyword, '%'))")
    Page<Customer> searchMyCustomers(@Param("tenantId") Long tenantId,
                                     @Param("userId") Long userId,
                                     @Param("keyword") String keyword,
                                     Pageable pageable);

    // 3. Check trùng phone phải check trong nội bộ Tenant thôi nhé
    boolean existsByPhoneAndTenantId(String phone, Long tenantId);

    // 4. Tìm chi tiết theo ID và Tenant (để tránh việc user Tenant A đoán ID của Tenant B)
    Optional<Customer> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Customer> findByPhone(String customerPhone);
}
