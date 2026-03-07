package org.example.event_platform.Repository;

import org.example.event_platform.Entity.AssignStatus;
import org.example.event_platform.Entity.UserEvent;
import org.example.event_platform.Entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserEventRepository extends JpaRepository<UserEvent, Long> {
    // Tìm tất cả suất diễn của 1 show
    List<UserEvent> findByEventId(Long eventId);

    // Tìm tất cả show mà 1 member được gán
    @Query("SELECT ue FROM UserEvent ue " +
            "JOIN FETCH ue.event e " +
            "LEFT JOIN FETCH e.assignedMembers " +
            "WHERE ue.user.id = :userId")
    List<UserEvent> findByUserId(@Param("userId")Long userId);

    @Query("SELECT COUNT(ue) FROM UserEvent ue WHERE ue.user.id = :userId AND ue.status = :status")
    long countCompletedShows(@Param("userId") Long userId,@Param("status") AssignStatus status);

    // Tìm đúng suất diễn của 1 người trong 1 show (để Member bấm xác nhận)
    Optional<UserEvent> findByEventIdAndUserId(Long eventId, Long userId);
}
