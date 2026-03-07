package org.example.event_platform.Controller;

import lombok.RequiredArgsConstructor;
import org.example.event_platform.Dto.Event.*;
import org.example.event_platform.Entity.User; // Class User của ông
import org.example.event_platform.Service.Event.EventService;
import org.example.event_platform.util.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tenant/events")
@RequiredArgsConstructor
public class TenantEventController {

    private final EventService eventService;

    /**
     * Lấy lịch diễn tháng hiện tại của đội
     * @paramcurrentUser: Lấy từ Spring Security Context để lấy tenantId
     */
    @GetMapping
    @PreAuthorize("hasAuthority('EVENT_VIEW')")
    public ResponseEntity<Page<EventResponse>> getMySchedule(
            @RequestParam int month,
            @RequestParam int year,
            @PageableDefault(size = 10) Pageable pageable) {

        // 1. Lấy tenantId từ cái TenantContext mà ông đã set ở Filter
        Long tenantId = TenantContext.getCurrentShopId();

//        log.info("Fetching schedule for Tenant ID from Context: {}", tenantId);

        // 2. Kiểm tra nếu không có tenantId (tránh NPE)
        if (tenantId == null) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(eventService.getTenantEvents(
                tenantId, month, year, pageable
        ));
    }

    /**
     * Lấy 3 cái card thống kê cuối trang (Tổng show, Doanh thu, Tỷ lệ)
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('EVENT_SUMMARY_VIEW')")
    public ResponseEntity<MonthlySummaryDto> getMySummary(
            @RequestParam int month,
            @RequestParam int year) {

        Long tenantId = TenantContext.getCurrentShopId();

        return ResponseEntity.ok(eventService.getTenantMonthlySummary(
                tenantId, month, year
        ));
    }

    @PostMapping("/accept/{eventId}")
    @PreAuthorize("hasAuthority('EVENT_MODERATE')")
    public ResponseEntity<String> acceptEvent(@PathVariable Long eventId) {
        Long tenantId = TenantContext.getCurrentShopId();
        eventService.acceptEvent(eventId,tenantId);
        return ResponseEntity.ok("success");
    }

    @PostMapping("/reject/{eventId}")
    @PreAuthorize("hasAuthority('EVENT_MODERATE')")
    public ResponseEntity<String> rejectEvent(@PathVariable Long eventId) {
        Long tenantId = TenantContext.getCurrentShopId();
        eventService.rejectEvent(eventId,tenantId);
        return ResponseEntity.ok("success");
    }

    /**
     * Xem chi tiết sự kiện của đội mình
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EVENT_DETAIL_VIEW')")
    public ResponseEntity<EventResponse> getMyEventDetail(
            @PathVariable Long id) {

        EventResponse event = eventService.getEventDetail(id);
        Long tenantId = TenantContext.getCurrentShopId();

        // Kiểm tra xem sự kiện này có đúng là của đội mình không
        if (!event.getTenantId().equals(tenantId)) {
            return ResponseEntity.status(403).build(); // Không được xem show của đội khác
        }

        return ResponseEntity.ok(event);
    }

    // ==========================================
    // SECTION 1: DÀNH CHO ADMIN ĐOÀN LÂN
    // ==========================================

    /**
     * Admin gán danh sách thành viên vào show diễn
     */
    @PostMapping("/{id}/assign")
    public ResponseEntity<String> assignMembers(
            @PathVariable Long id,
            @RequestBody List<AssignMemberRequest> requests) {
        eventService.assignMembersToEvent(id, requests);
        return ResponseEntity.ok("Đã gán thành viên vào show thành công!");
    }

    /**
     * Lấy chi tiết show kèm theo mảng thành viên (Gộp mảng)
     */
    @GetMapping("/{id}/with-members")
    public ResponseEntity<EventWithMembersResponse> getEventWithMembers(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventDetailWithMembers(id));
    }


    // ==========================================
    // SECTION 2: DÀNH CHO MEMBER (ANH EM ĐI DIỄN)
    // ==========================================

    /**
     * Member lấy danh sách các show mình được gán (Tab Lịch Diễn)
     */
    @GetMapping("/my-assignments/{userId}")
    public ResponseEntity<List<UserEventDTO>> getMyAssignments(@PathVariable Long userId) {
        return ResponseEntity.ok(eventService.getMyAssignedEvents(userId));
    }

    /**
     * Member bấm Xác nhận (ACCEPTED) hoặc Từ chối (REJECTED)
     */
    @PatchMapping("/assignments/{userEventId}/respond")
    public ResponseEntity<String> respondToAssignment(
            @PathVariable Long userEventId,
            @RequestParam String status,
            @RequestParam(required = false) String note) {
        eventService.respondToAssignment(userEventId, status, note);
        return ResponseEntity.ok("Phản hồi trạng thái thành công!");
    }

    /**
     * Member bấm Check-in tại điểm diễn
     */
    @PostMapping("/assignments/{userEventId}/check-in")
    public ResponseEntity<String> checkIn(
            @PathVariable Long userEventId,
            @RequestParam String location) {
        eventService.checkIn(userEventId, location);
        return ResponseEntity.ok("Check-in thành công!");
    }

    /**
     * Member bấm Check-out khi diễn xong
     */
    @PostMapping("/assignments/{userEventId}/check-out")
    public ResponseEntity<String> checkOut(@PathVariable Long userEventId) {
        eventService.checkOut(userEventId);
        return ResponseEntity.ok("Check-out thành công! Hoàn thành suất diễn.");
    }

    @PatchMapping("/{id}/concentrate-info")
    public ResponseEntity<EventResponse> updateConcentrate(
            @PathVariable Long id,
            @RequestBody UpdateConcentrate concentrateDto) {

        EventResponse updated = eventService.updateConcentrateInfo(id, concentrateDto);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/concentrate-check-in")
    public ResponseEntity<String> concentrateCheckIn(
            @PathVariable Long id,
            @RequestParam String location) {
        String msg = eventService.concentrateCheckIn(id, location);
        return ResponseEntity.ok(msg);
    }

//    @GetMapping("/my-stats")
//    public ResponseEntity<Map<String, Object>> getMyStats(@PathVariable Long userId) {
//        return ResponseEntity.ok(userService.getMemberStats(userId));
//    }
}
