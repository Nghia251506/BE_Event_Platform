package org.example.event_platform.Repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.example.event_platform.Entity.Event;
import org.example.event_platform.Entity.EventStatus;
import org.example.event_platform.Entity.EventType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class EventSpecification {

    public static Specification<Event> filterEvents(
            String search,
            EventStatus status,
            EventType type,
            Long tenantId,
            LocalDate startDate,
            LocalDate endDate) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Search theo tên sự kiện hoặc tên khách hàng (Ô Search chính)
            if (search != null && !search.isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), searchPattern),
                        cb.like(cb.lower(root.get("customerName")), searchPattern)
                ));
            }

            // 2. Lọc theo trạng thái (Select box)
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // 3. Lọc theo loại sự kiện
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            // 4. Lọc theo Đội lân (Tenant) - Quan trọng cho Admin sàn
            if (tenantId != null) {
                predicates.add(cb.equal(root.get("tenant").get("id"), tenantId));
            }

            // 5. Lọc theo khoảng ngày (Từ ngày... Đến ngày...)
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), endDate));
            }

            // Sắp xếp mặc định: Show mới nhất lên đầu
            query.orderBy(cb.desc(root.get("eventDate")), cb.desc(root.get("startTime")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
