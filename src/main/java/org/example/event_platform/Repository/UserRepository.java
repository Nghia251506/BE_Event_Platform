package org.example.event_platform.Repository;

import org.example.event_platform.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.roles r " +
            "LEFT JOIN FETCH r.permissions " +
            "WHERE u.username = :username")
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    long countByIsActiveTrue();

    List<User> findByIsActiveTrue();
    @Query("SELECT u FROM User u " +
            "JOIN FETCH u.roles r " +
            "JOIN FETCH u.tenant t " +
            "WHERE u.tenant.id = :shopId AND r.id = :roleId")
    Optional<User> findByTenantIdAndRoleId(@Param("tenantId") Long tenantId, @Param("roleId") Long roleId);
    Optional<User> findByTenantIdAndEmail(Long tenantId, String email);
    // Lấy danh sách nhân viên của một Tenant (Quan trọng cho Multi-tenant)
    Page<User> findByTenantId(Long tenantId, Pageable pageable);

    // Tìm nhân viên cụ thể trong 1 Tenant (Đảm bảo Admin tenant A không sửa được user tenant B)
    Optional<User> findByIdAndTenantId(Long id, Long tenantId);

    // Check trùng email/phone khi tạo nhân viên mới
    boolean existsByEmailAndTenantId(String email, Long tenantId);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.roles r " +
            "LEFT JOIN FETCH r.permissions " +
            "LEFT JOIN FETCH u.permissions " +
            "WHERE u.id = :id")
    Optional<User> findById(@Param("id") Long id);
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.tenant.id = :tenantId")
    List<User> findAllMembersByTenant(Long tenantId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.tenant.id = :tenantId AND u.status = 'ACTIVE'")
    Long countActiveUsersByTenant(Long tenantId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.tenant.id = :tenantId")
    Long countTotalUsersByTenant(Long tenantId);
}
