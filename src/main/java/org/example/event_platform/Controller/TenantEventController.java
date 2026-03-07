package org.example.event_platform.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.event_platform.Dto.Event.*;
import org.example.event_platform.Entity.AssignStatus;
import org.example.event_platform.Entity.EventStatus;
import org.example.event_platform.Entity.EventType;
import org.example.event_platform.Entity.User; // Class User của ông
import org.example.event_platform.Service.Event.EventService;
import org.example.event_platform.util.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tenant/events")
@RequiredArgsConstructor
@Slf4j
public class TenantEventController {

    private final EventService eventService;

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('EVENT_VIEW')")
    public ResponseEntity<Page<EventResponse>> getAllMyEvents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) EventType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "eventDate") Pageable pageable) {

        // ÉP CỨNG: Chỉ lấy data của đội mình từ Context
        Long tenantId = TenantContext.getCurrentShopId();
        if (tenantId == null) return ResponseEntity.status(403).build();

        return ResponseEntity.ok(eventService.getAllEvents(
                search, status, type, tenantId, startDate, endDate, pageable
        ));
    }

    /**
     * 2. Xem chi tiết một sự kiện bất kỳ (Có check bảo mật)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EVENT_DETAIL_VIEW')")
    public ResponseEntity<EventResponse> getMyEventDetail(@PathVariable Long id) {
        EventResponse event = eventService.getEventDetail(id);
        Long tenantId = TenantContext.getCurrentShopId();

        // Bảo mật: Show này phải thuộc về đội của Admin đang đăng nhập
        if (event.getTenantId() == null || !event.getTenantId().equals(tenantId)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(event);
    }

    // ==========================================
    // SECTION 1: QUẢN LÝ SHOW (DÀNH CHO ADMIN ĐỘI LÂN)
    // ==========================================

    /**
     * Lấy lịch diễn tháng của đội (Dùng TenantContext)
     */
    @GetMapping
    @PreAuthorize("hasAuthority('EVENT_VIEW')")
    public ResponseEntity<Page<EventResponse>> getMySchedule(
            @RequestParam int month,
            @RequestParam int year,
            @PageableDefault(size = 10) Pageable pageable) {

        Long tenantId = TenantContext.getCurrentShopId();
        if (tenantId == null) return ResponseEntity.status(403).build();

        return ResponseEntity.ok(eventService.getTenantEvents(tenantId, month, year, pageable));
    }

    /**
     * Card thống kê tháng (Tổng show, Doanh thu, Tỷ lệ hoàn thành)
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('EVENT_SUMMARY_VIEW')")
    public ResponseEntity<MonthlySummaryDto> getMySummary(
            @RequestParam int month,
            @RequestParam int year) {

        Long tenantId = TenantContext.getCurrentShopId();
        return ResponseEntity.ok(eventService.getTenantMonthlySummary(tenantId, month, year));
    }

    /**
     * Chấp nhận show từ sàn đẩy về
     */
    @PostMapping("/{eventId}/accept")
    @PreAuthorize("hasAuthority('EVENT_MODERATE')")
    public ResponseEntity<String> acceptEvent(@PathVariable Long eventId) {
        Long tenantId = TenantContext.getCurrentShopId();
        eventService.acceptEvent(eventId, tenantId);
        return ResponseEntity.ok("Đã nhận show thành công! Hãy gán anh em đi diễn.");
    }

    /**
     * Từ chối show từ sàn (Trả lại cho Admin sàn)
     */
    @PostMapping("/{eventId}/reject")
    @PreAuthorize("hasAuthority('EVENT_MODERATE')")
    public ResponseEntity<String> rejectEvent(@PathVariable Long eventId) {
        Long tenantId = TenantContext.getCurrentShopId();
        eventService.rejectEvent(eventId, tenantId);
        return ResponseEntity.ok("Đã từ chối show.");
    }

    /**
     * Admin gán danh sách thành viên vào show (Bắn FCM tự động trong Service)
     */
    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('EVENT_ASSIGN')")
    public ResponseEntity<String> assignMembers(
            @PathVariable Long id,
            @RequestBody List<AssignMemberRequest> requests) {
        eventService.assignMembersToEvent(id, requests);
        return ResponseEntity.ok("Đã gán thành viên và thông báo cho anh em!");
    }

    /**
     * Cập nhật thông tin tập trung (Giờ giấc, địa điểm tập trung)
     */
    @PatchMapping("/{id}/concentrate-info")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> updateConcentrate(
            @PathVariable Long id,
            @RequestBody UpdateConcentrate concentrateDto) {
        return ResponseEntity.ok(eventService.updateConcentrateInfo(id, concentrateDto));
    }

    /**
     * Chi tiết show kèm danh sách anh em tham gia
     */
    @GetMapping("/{id}/with-members")
    @PreAuthorize("hasAuthority('EVENT_DETAIL_VIEW')")
    public ResponseEntity<EventWithMembersResponse> getEventWithMembers(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventDetailWithMembers(id));
    }

    // ==========================================
    // SECTION 2: DÀNH CHO MEMBER (ANH EM ĐI DIỄN)
    // ==========================================

    /**
     * Lấy danh sách show ĐƯỢC GÁN của chính mình
     * Note: Dùng userId từ Token để bảo mật
     */
    @GetMapping("/my-assignments")
    public ResponseEntity<List<UserEventDTO>> getMyAssignments(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(eventService.getMyAssignedEvents(currentUser.getId()));
    }

    /**
     * Member Chấp nhận hoặc Từ chối show
     */
    @PatchMapping("/assignments/{userEventId}/respond")
    public ResponseEntity<String> respondToAssignment(
            @PathVariable Long userEventId,
            @RequestParam AssignStatus status, // Dùng thẳng Enum cho chuẩn
            @RequestParam(required = false) String note) {
        eventService.respondToAssignment(userEventId, status, note);
        return ResponseEntity.ok("Đã phản hồi trạng thái show.");
    }

    /**
     * Check-in tập trung
     */
    @PostMapping("/assignments/{userEventId}/concentrate-check-in")
    public ResponseEntity<String> concentrateCheckIn(
            @PathVariable Long userEventId,
            @RequestParam String location) {
        return ResponseEntity.ok(eventService.concentrateCheckIn(userEventId, location));
    }

    /**
     * Check-in tại điểm diễn (Tự động nhảy status Event sang IN_PROGRESS)
     */
    @PostMapping("/assignments/{userEventId}/check-in")
    public ResponseEntity<String> checkIn(
            @PathVariable Long userEventId,
            @RequestParam String location) {
        eventService.checkIn(userEventId, location);
        return ResponseEntity.ok("Check-in thành công! Chúc ông giáo diễn tốt.");
    }

    /**
     * Check-out khi xong show (Tự động đóng show nếu là người cuối cùng)
     */
    @PostMapping("/assignments/{userEventId}/check-out")
    public ResponseEntity<String> checkOut(@PathVariable Long userEventId) {
        String msg = eventService.checkOut(userEventId);
        return ResponseEntity.ok(msg);
    }
}
