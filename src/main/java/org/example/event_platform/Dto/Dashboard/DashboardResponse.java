package org.example.event_platform.Dto.Dashboard;

import lombok.*;

import java.util.List;

@Data
@Builder
public class DashboardResponse {
    // 1. Bộ 4 con số thống kê ở trên cùng
    private Long monthlyEvents;      // Lịch diễn tháng này
    private String activeMemberRatio; // Tỷ lệ thành viên (VD: "8/10")
    private Double monthlyRevenue;    // Thu nhập tháng
    private Double averageRating;     // Đánh giá TB

    // 2. Danh sách sự kiện sắp tới (Tận dụng Event DTO có sẵn)
    private List<UpcomingEventDTO> upcomingEvents;

    // 3. Trạng thái đội ngũ (User DTO rút gọn)
    private List<MemberStatusDTO> teamStatus;
}
