package org.example.event_platform.Controller;

import lombok.RequiredArgsConstructor;
import org.example.event_platform.Dto.Dashboard.DashboardResponse;
import org.example.event_platform.Entity.User;
import org.example.event_platform.Service.Dashboard.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * LẤY DỮ LIỆU TỔNG QUAN (STATS, UPCOMING EVENTS, TEAM STATUS)
     * Dùng cho trang Dashboard chính của cả React và Flutter
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ResponseEntity<DashboardResponse> getDashboardSummary(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(dashboardService.getSummary(currentUser));
    }
}