package org.example.event_platform.Dto.Dashboard;

public record DashboardStatDTO(
        long totalShows,      // Tổng show đã diễn (CHECKED_OUT)
        long pendingShows,    // Show đã nhận nhưng chưa diễn (ACCEPTED, CHECKIN_CONCENTRATE)
        double totalEarnings, // Tổng thu nhập
        String rank           // Hạng (ví dụ: Chiến thần, Newbie...) - cho ae thích thú
) {}
