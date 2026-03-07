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

    // 1. Tìm kiếm theo Keyword (Tên hoặc SĐT) + Phân trang
    // Nếu keyword null hoặc trống, nó sẽ lấy tất cả nhờ logic ( :keyword IS NULL OR ... )
    @Query("SELECT c FROM Customer c WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "c.phone LIKE CONCAT('%', :keyword, '%'))")
    Page<Customer> searchCustomers(@Param("keyword") String keyword, Pageable pageable);

    // 2. Nếu ông muốn Admin xem hết, còn Member chỉ xem khách của mình:
    @Query("SELECT c FROM Customer c WHERE " +
            "c.assignedTo.id = :userId AND " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "c.phone LIKE CONCAT('%', :keyword, '%'))")
    Page<Customer> searchMyCustomers(@Param("userId") Long userId,
                                     @Param("keyword") String keyword,
                                     Pageable pageable);

    boolean existsByPhone(String phone);

    Optional<Customer> findByPhone(String customerPhone);
}
