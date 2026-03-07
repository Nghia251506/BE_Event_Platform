package org.example.event_platform.Repository;

import org.example.event_platform.Entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    // Tìm theo tên để dùng khi gán quyền hoặc check logic
    Optional<Permission> findByName(String name);
    @Query("SELECT p FROM Permission p WHERE p.tenant.id IS NULL OR p.tenant.id = :tenantId")
    List<Permission> findAllByTenantIdIsNullOrderByTenantId(Long tenantId);
    @Query("SELECT p FROM Permission p WHERE p.tenant.id IS NULL OR p.tenant.id = :tenantId")
    List<Permission> findGlobalAndTenantPermissions(@Param("tenantId") Long tenantId);
}