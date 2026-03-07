package org.example.event_platform.Service.Dashboard;

import lombok.RequiredArgsConstructor;
import org.example.event_platform.Dto.Dashboard.DashboardResponse;
import org.example.event_platform.Dto.Dashboard.MemberStatusDTO;
import org.example.event_platform.Dto.Dashboard.UpcomingEventDTO;
import org.example.event_platform.Entity.User;
import org.example.event_platform.Repository.EventRepository;
import org.example.event_platform.Repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public DashboardResponse getSummary(User currentUser) { // Truyền User đang login vào đây
        Long tenantId = currentUser.getTenant().getId();

        // 1. Lấy thống kê theo Tenant
        Long monthlyEvents = eventRepository.countEventsByTenantThisMonth(tenantId);
        Double revenue = eventRepository.calculateMonthlyRevenueByTenant(tenantId);
        Long activeUsers = userRepository.countActiveUsersByTenant(tenantId); // Nên dùng UserRepo
        Long totalUsers = userRepository.countTotalUsersByTenant(tenantId);

        // 2. Lấy 5 show sắp tới theo Tenant
        List<UpcomingEventDTO> upcomingDTOs = eventRepository.findUpcomingEventsByTenant(tenantId, PageRequest.of(0, 5))
                .stream()
                .map(event -> {
                    UpcomingEventDTO dto = new UpcomingEventDTO();
                    dto.setId(event.getId());
                    dto.setName(event.getName());
                    dto.setClientName(event.getCustomer().getFullName());
                    dto.setStartTime(LocalDateTime.from(event.getStartTime()));
                    dto.setLocation(event.getLocation());
                    dto.setStatus(event.getStatus().toString());
                    dto.setPrice(event.getTotalAmount());
                    return dto;
                }).collect(Collectors.toList());

        // 3. Lấy trạng thái anh em theo Tenant
        List<MemberStatusDTO> memberDTOs = userRepository.findAllMembersByTenant(tenantId)
                .stream()
                .map(user -> {
                    MemberStatusDTO dto = new MemberStatusDTO();
                    dto.setFullName(user.getFullName());
                    dto.setRoleName(user.getRoles().getName());
                    dto.setStatus(user.getStatus().toString());
                    return dto;
                }).collect(Collectors.toList());

        return DashboardResponse.builder()
                .monthlyEvents(monthlyEvents)
                .activeMemberRatio(activeUsers + "/" + totalUsers)
                .monthlyRevenue(revenue != null ? revenue : 0.0)
                .averageRating(4.9)
                .upcomingEvents(upcomingDTOs)
                .teamStatus(memberDTOs)
                .build();
    }
}