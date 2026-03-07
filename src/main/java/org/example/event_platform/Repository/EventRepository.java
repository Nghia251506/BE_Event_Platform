package org.example.event_platform.Repository;

import org.example.event_platform.Entity.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    @Query("SELECT COUNT(e) FROM Event e WHERE e.tenant.id = :tenantId " +
            "AND MONTH(e.startTime) = MONTH(CURRENT_DATE) " +
            "AND YEAR(e.startTime) = YEAR(CURRENT_DATE)")
    Long countEventsByTenantThisMonth(Long tenantId);

    @Query("SELECT SUM(e.totalAmount) FROM Event e WHERE e.tenant.id = :tenantId " +
            "AND MONTH(e.startTime) = MONTH(CURRENT_DATE) " +
            "AND YEAR(e.startTime) = YEAR(CURRENT_DATE) AND e.status = 'CONFIRMED'")
    Double calculateMonthlyRevenueByTenant(Long tenantId);

    @Query("SELECT e FROM Event e WHERE e.tenant.id = :tenantId " +
            "AND e.startTime >= CURRENT_TIMESTAMP AND e.status != 'CANCELLED' ORDER BY e.startTime ASC")
    List<Event> findUpcomingEventsByTenant(Long tenantId, Pageable pageable);
}
