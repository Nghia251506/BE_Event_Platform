//package org.example.event_platform.Controller;
//
//import lombok.RequiredArgsConstructor;
//import org.example.event_platform.Dto.Event.EventRequest;
//import org.example.event_platform.Dto.Event.EventResponse;
//import org.example.event_platform.Entity.EventStatus;
//import org.example.event_platform.Entity.EventType;
//import org.example.event_platform.Service.Event.EventService;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.web.PageableDefault;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDate;
//
//@RestController
//@RequestMapping("/api/platform/events")
//@RequiredArgsConstructor
//public class EventController {
//
//    private final EventService eventService;
//
//    /**
//     * API Lấy danh sách sự kiện (Phân trang + Lọc động)
//     * URL ví dụ: /api/platform/events?search=khai+truong&status=SCHEDULED&page=0&size=10
//     */
//    @GetMapping
////    @PreAuthorize("hasAuthority('GET_ALL_EVENT')")
//    public ResponseEntity<Page<EventResponse>> getAllEvents(
//            @RequestParam(required = false) String search,
//            @RequestParam(required = false) EventStatus status,
//            @RequestParam(required = false) EventType type,
//            @RequestParam(required = false) Long tenantId,
//            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate startDate,
//            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate endDate,
//            @PageableDefault(size = 10, sort = "eventDate") Pageable pageable) {
//
//        return ResponseEntity.ok(eventService.getAllEvents(
//                search, status, type, tenantId, startDate, endDate, pageable
//        ));
//    }
//
//    /**
//     * Lấy chi tiết một sự kiện
//     */
//    @GetMapping("/{id}")
////    @PreAuthorize("hasAuthority('GET_EVENT_DETAIL')")
//    public ResponseEntity<EventResponse> getEventDetail(@PathVariable Long id) {
//        return ResponseEntity.ok(eventService.getEventDetail(id));
//    }
//
//    /**
//     * Tạo sự kiện mới
//     */
////    @PostMapping
////    public ResponseEntity<EventResponse> createEvent(@RequestBody EventRequest request) {
////        return ResponseEntity.ok(eventService.createEvent(request));
////    }
//
//    /**
//     * Cập nhật trạng thái sự kiện (Xác nhận hoàn thành, hủy, v.v.)
//     */
//    @PatchMapping("/{id}/status")
////    @PreAuthorize("hasAuthority('UPDATE_EVENT_STATUS')")
//    public ResponseEntity<EventResponse> updateStatus(
//            @PathVariable Long id,
//            @RequestParam EventStatus status) {
//        return ResponseEntity.ok(eventService.updateStatus(id, status));
//    }
//}