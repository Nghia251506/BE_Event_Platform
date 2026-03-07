package org.example.event_platform.Controller;
import lombok.RequiredArgsConstructor;
import org.example.event_platform.Dto.Event.EventRequest;
import org.example.event_platform.Dto.Event.EventResponse;
import org.example.event_platform.Entity.EventStatus;
import org.example.event_platform.Entity.EventType;
import org.example.event_platform.Entity.User;
import org.example.event_platform.Service.Event.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {
    private final EventService eventService;

    /**
     * Tạo sự kiện mới
     */
    @PostMapping("/events")
    public ResponseEntity<EventResponse> createEvent(
            @RequestBody EventRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(eventService.createEvent(request, currentUser));
    }
}
