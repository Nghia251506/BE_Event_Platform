package org.example.event_platform.Service.Event;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.event_platform.Dto.Dashboard.DashboardStatDTO;
import org.example.event_platform.Dto.Event.*;
import org.example.event_platform.Entity.*;
import org.example.event_platform.Mapper.EventMapper;
import org.example.event_platform.Mapper.UserEventMapper;
import org.example.event_platform.Repository.*;
import org.example.event_platform.Repository.specification.EventSpecification;
import org.example.event_platform.Service.FCM.NotificationManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final UserEventMapper userEventMapper;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final UserEventRepository userEventRepository;
    private final CustomerRepository customerRepository;
    private final NotificationManager notificationManager;

    /**
     * 1. Lấy danh sách sự kiện kèm bộ lọc nâng cao cho Admin sàn
     */
    @Transactional(readOnly = true)
    public Page<EventResponse> getAllEvents(
            String search, EventStatus status, EventType type,
            Long tenantId, LocalDate startDate, LocalDate endDate, Pageable pageable) {

        Specification<Event> spec = EventSpecification.filterEvents(
                search, status, type, tenantId, startDate, endDate
        );
        return eventRepository.findAll(spec, pageable).map(eventMapper::toResponse);
    }

    /**
     * 2. Xem chi tiết 1 sự kiện
     */
    @Transactional(readOnly = true)
    public EventResponse getEventDetail(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sự kiện với ID: " + id));
        return eventMapper.toResponse(event);
    }

    /**
     * 3. Tạo sự kiện mới (Xử lý Role & Customer Triệt để)
     */
    @Transactional
    public EventResponse createEvent(EventRequest request, User currentUser) {
        Event event = eventMapper.toEntity(request);

        // Xử lý Customer (Tránh trùng lặp Database)
        Customer finalCustomer = processCustomerLogic(request);
        event.setCustomer(finalCustomer);

        if (currentUser != null) {
            String roleName = currentUser.getRoles().getName();
             if ("ADMIN".equals(roleName)) {
                event.setPlatformFee(BigDecimal.ZERO);
                event.setStatus(EventStatus.SCHEDULED);
                event.setTenant(currentUser.getTenant());
            } else {
                calculateDefaultFee(event);
                event.setStatus(EventStatus.SCHEDULED);
            }
            event.setCreatedBy(currentUser.getUsername());
        } else {
            calculateDefaultFee(event);
            event.setStatus(EventStatus.SCHEDULED);
            event.setCreatedBy("GUEST");
        }

        return eventMapper.toResponse(eventRepository.save(event));
    }

    private Customer processCustomerLogic(EventRequest request) {
        if (request.getCustomerId() != null) {
            return customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException("Khách hàng không tồn tại!"));
        }
        return customerRepository.findByPhone(request.getCustomerPhone())
                .orElseGet(() -> customerRepository.save(Customer.builder()
                        .fullName(request.getCustomerName())
                        .phone(request.getCustomerPhone())
                        .build()));
    }

    private void calculateDefaultFee(Event event) {
        BigDecimal total = event.getTotalAmount();
        event.setPlatformFee(total != null ? total.multiply(new BigDecimal("0.1")) : BigDecimal.ZERO);
    }

    /**
     * 4. Admin gán thành viên & Bắn FCM "Rung túi"
     */
    @Transactional
    public void assignMembersToEvent(Long eventId, List<AssignMemberRequest> requests) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Sự kiện không tồn tại"));

        for (AssignMemberRequest req : requests) {
            UserEvent userEvent = userEventRepository.findByEventIdAndUserId(eventId, req.getUserId())
                    .orElse(new UserEvent());

            userEvent.setEvent(event);
            userEvent.setUser(userRepository.findById(req.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("User " + req.getUserId() + " không tồn tại")));
            userEvent.setPosition(req.getPosition());
            userEvent.setStatus(AssignStatus.PENDING);

            userEventRepository.save(userEvent);

            // 🔥 FCM: Thông báo gán show mới
            notificationManager.notifyMemberAssigned(req.getUserId(), event.getName(), req.getPosition());
        }

        // Chuyển status event sang CONFIRMED khi bắt đầu có nhân sự
        if (event.getStatus() == EventStatus.SCHEDULED) {
            event.setStatus(EventStatus.CONFIRMED);
            eventRepository.save(event);
        }
    }

    /**
     * 5. Member phản hồi: Chấp nhận hoặc Từ chối
     */
    @Transactional
    public void respondToAssignment(Long userEventId, AssignStatus status, String note) {
        UserEvent ue = userEventRepository.findById(userEventId)
                .orElseThrow(() -> new EntityNotFoundException("Không thấy bản ghi phân công"));

        ue.setStatus(status);
        ue.setNote(note);
        ue.setRespondedAt(LocalDateTime.now());
        userEventRepository.save(ue);

        // 🔥 FCM: Báo Admin kết quả trả lời
        Long adminId = (ue.getEvent().getTenant() != null) ? ue.getEvent().getTenant().getId() : 1L; // Fallback admin sàn
        if (status == AssignStatus.REJECTED) {
            notificationManager.notifyAdminMemberRejected(ue.getUser().getFullName(), ue.getEvent().getName(), note);
        } else {
            notificationManager.notifyAdminMemberAccepted(ue.getUser().getFullName(), ue.getEvent().getName());
        }
    }

    /**
     * 6. Check-in: Tự động khởi chạy Show (IN_PROGRESS)
     */
    @Transactional
    public void checkIn(Long userEventId, String location) {
        UserEvent ue = userEventRepository.findById(userEventId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy lịch diễn"));

        if (ue.getStatus() != AssignStatus.ACCEPTED && ue.getStatus() != AssignStatus.CHECKIN_CONCENTRATE) {
            throw new IllegalStateException("Bạn phải xác nhận tham gia hoặc hoàn thành báo danh tập trung trước khi Check-in.");
        }

        ue.setCheckinAt(LocalTime.now());
        ue.setCheckinLocation(location);
        ue.setStatus(AssignStatus.CHECKED_IN);
        userEventRepository.save(ue);

        // 🔥 Tự động chuyển Show sang IN_PROGRESS nếu là người đầu tiên check-in
        Event event = ue.getEvent();
        if (event.getStatus() == EventStatus.CONFIRMED) {
            event.setStatus(EventStatus.IN_PROGRESS);
            eventRepository.save(event);
        }
    }

    /**
     * 7. Check-out: Tự động đóng Show (COMPLETED)
     */
    @Transactional
    public String checkOut(Long userEventId) {
        UserEvent ue = userEventRepository.findById(userEventId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy lịch diễn"));

        if (ue.getStatus() != AssignStatus.CHECKED_IN) {
            throw new IllegalStateException("Bạn chưa Check-in điểm diễn!");
        }

        ue.setCheckoutAt(LocalTime.now());
        ue.setStatus(AssignStatus.COMPLETED);
        userEventRepository.save(ue);

        // 🔥 KIỂM TRA ĐÓNG SHOW TỰ ĐỘNG
        autoCompleteEventIfFinished(ue.getEvent().getId());

        return "Chúc mừng bạn đã hoàn thành show diễn!";
    }

    private void autoCompleteEventIfFinished(Long eventId) {
        List<UserEvent> members = userEventRepository.findByEventId(eventId);

        boolean isFinished = members.stream()
                .filter(m -> m.getStatus() != AssignStatus.REJECTED)
                .allMatch(m -> m.getStatus() == AssignStatus.COMPLETED);

        if (isFinished) {
            Event event = eventRepository.findById(eventId).get();
            event.setStatus(EventStatus.COMPLETED);
            eventRepository.save(event);

            // 🔥 FCM: Báo cho dàn Admin của đoàn hiện tại
            notificationManager.notifyAdminsOfCurrentTenant(
                    "EVENT_AUTO_COMPLETED",
                    "🎊 Show [" + event.getName() + "] đã hoàn thành xuất sắc!",
                    "success_ding.mp3"
            );
        }
    }

    /**
     * 8. Thống kê & Lịch diễn (Giữ nguyên logic cũ nhưng clean code)
     */
    @Transactional(readOnly = true)
    public MonthlySummaryDto getTenantMonthlySummary(Long tenantId, int month, int year) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        Specification<Event> spec = EventSpecification.filterEvents(null, null, null, tenantId, start, end);
        List<Event> events = eventRepository.findAll(spec);

        BigDecimal revenue = events.stream()
                .filter(e -> e.getStatus() != EventStatus.CANCELLED)
                .map(Event::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completed = events.stream().filter(e -> e.getStatus() == EventStatus.COMPLETED).count();
        double rate = events.isEmpty() ? 0 : (double) completed / events.size() * 100;

        return MonthlySummaryDto.builder()
                .totalEvents(events.size())
                .estimatedRevenue(revenue)
                .completionRate(rate)
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserEventDTO> getMyAssignedEvents(Long userId) {
        return userEventRepository.findByUserId(userId).stream()
                .map(userEventMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventWithMembersResponse getEventDetailWithMembers(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EntityNotFoundException("Show không tồn tại"));
        List<UserEventDTO> members = userEventRepository.findByEventId(eventId).stream()
                .map(userEventMapper::toDto).collect(Collectors.toList());
        return new EventWithMembersResponse(eventMapper.toResponse(event), members);
    }

    /**
     * Lấy danh sách show diễn của riêng từng đội lân (Tenant) theo tháng/năm
     */
    @Transactional(readOnly = true)
    public Page<EventResponse> getTenantEvents(Long tenantId, int month, int year, Pageable pageable) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        Specification<Event> spec = EventSpecification.filterEvents(
                null, null, null, tenantId, startOfMonth, endOfMonth
        );

        return eventRepository.findAll(spec, pageable).map(eventMapper::toResponse);
    }

    @Transactional
    public void acceptEvent(Long eventId, Long tenantId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sự kiện"));

        // Kiểm tra quyền sở hữu show
        if (event.getTenant() == null || !event.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Bạn không có quyền chấp nhận show này!");
        }

        event.setStatus(EventStatus.CONFIRMED);
        eventRepository.save(event);
    }

    @Transactional
    public void rejectEvent(Long eventId, Long tenantId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sự kiện"));

        if (event.getTenant() == null || !event.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Bạn không có quyền từ chối show này!");
        }

        event.setStatus(EventStatus.CANCELLED); // Hoặc logic của ông là đẩy lại lên sàn thì set Tenant = null
        eventRepository.save(event);
    }

    @Transactional
    public EventResponse updateConcentrateInfo(Long eventId, UpdateConcentrate updateDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy show diễn"));

        eventMapper.updateConcentrate(updateDto, event);
        Event savedEvent = eventRepository.save(event);

        // 🔥 FCM: Báo cho tất cả thành viên đã ACCEPTED trong show này
        List<UserEvent> assignedMembers = userEventRepository.findByEventId(eventId);
        assignedMembers.stream()
                .filter(m -> m.getStatus() == AssignStatus.ACCEPTED || m.getStatus() == AssignStatus.PENDING)
                .forEach(m -> {
                    notificationManager.notifyMemberConcentrate(
                            m.getUser().getId(),
                            savedEvent.getName(),
                            String.valueOf(updateDto.getConcentrateTime()),
                            updateDto.getConcentrateLocation()
                    );
                });

        return eventMapper.toResponse(savedEvent);
    }

    @Transactional
    public String concentrateCheckIn(Long userEventId) {
        // 1. Tìm bản ghi gán quân
        UserEvent ue = userEventRepository.findById(userEventId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy lịch diễn"));

        // 2. Lấy thời gian thực tế
        LocalTime now = LocalTime.now();
        ue.setActualConcentrateAt(now);
        ue.setStatus(AssignStatus.CHECKIN_CONCENTRATE);

        // 3. Lấy giờ quy định
        LocalTime scheduledTime = ue.getEvent().getConcentrateTime();

        String message;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        if (scheduledTime == null) {
            message = "Xác nhận có mặt thành công lúc " + now.format(formatter);
        } else {
            // 4. Logic so sánh giờ
            if (now.isBefore(scheduledTime) || now.equals(scheduledTime)) {
                message = "Xác nhận: Có mặt ĐÚNG GIỜ (" + now.format(formatter) + ").";
            } else {
                long minutesLate = java.time.Duration.between(scheduledTime, now).toMinutes();
                message = "Xác nhận: Có mặt MUỘN " + minutesLate + " phút (Giờ quy định: " + scheduledTime.format(formatter) + ").";
            }
        }

        userEventRepository.save(ue);
        return message;
    }
    @Transactional
    public DashboardStatDTO getMemberDashboardStats(Long tenantId, Long userId) {
        long finished = userEventRepository.countFinishedShows(tenantId, userId);
        long pending = userEventRepository.countPendingShows(tenantId, userId);
        Double totalEarnings = userEventRepository.sumTotalEarnings(tenantId, userId);

        // Xử lý nếu chưa có đồng nào (tránh Null)
        double earnings = (totalEarnings != null) ? totalEarnings : 0.0;

        return new DashboardStatDTO(
                finished,
                pending,
                earnings,
                calculateRank(finished)
        );
    }

    private String calculateRank(long finished) {
        if (finished > 50) return "Kim Cương";
        if (finished > 20) return "Bạch Kim";
        return "NEWBIE";
    }
}