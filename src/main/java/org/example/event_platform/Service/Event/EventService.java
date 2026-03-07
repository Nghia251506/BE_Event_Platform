package org.example.event_platform.Service.Event;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.event_platform.Dto.Event.*;
import org.example.event_platform.Entity.*;
import org.example.event_platform.Mapper.EventMapper;
import org.example.event_platform.Mapper.UserEventMapper;
import org.example.event_platform.Repository.EventRepository;
import org.example.event_platform.Repository.TenantRepository;
import org.example.event_platform.Repository.UserEventRepository;
import org.example.event_platform.Repository.UserRepository;
import org.example.event_platform.Repository.specification.EventSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    /**
     * Lấy danh sách sự kiện kèm bộ lọc nâng cao cho Admin sàn
     */
    @Transactional(readOnly = true)
    public Page<EventResponse> getAllEvents(
            String search,
            EventStatus status,
            EventType type,
            Long tenantId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        log.info("Admin fetching events with filter - Search: {}, Status: {}, Tenant: {}", search, status, tenantId);

        Specification<Event> spec = EventSpecification.filterEvents(
                search, status, type, tenantId, startDate, endDate
        );

        return eventRepository.findAll(spec, pageable)
                .map(eventMapper::toResponse);
    }

    /**
     * Xem chi tiết 1 sự kiện
     */
    @Transactional(readOnly = true)
    public EventResponse getEventDetail(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện với ID: " + id));
        return eventMapper.toResponse(event);
    }

    /**
     * Tạo sự kiện mới (Nếu Admin sàn được quyền tạo hộ)
     */
    @Transactional
    public EventResponse createEvent(EventRequest request, User currentUser) {
        Event event = eventMapper.toEntity(request);

        // 1. Phân luồng logic
        if (currentUser != null) {
            // TRƯỜNG HỢP CÓ ĐĂNG NHẬP
            String roleName = currentUser.getRoles().getName();

            if ("SUPER_ADMIN".equals(roleName)) {
                // Admin tạo: Lấy phí từ request, auto duyệt
                BigDecimal fee = (request.getPlatformFee() != null) ? request.getPlatformFee() : BigDecimal.ZERO;
                event.setPlatformFee(fee);
                event.setStatus(EventStatus.ACCEPTED);
            }
            else if ("ADMIN".equals(roleName)) {
                // Tenant tạo: Phí = 0, auto duyệt
                event.setPlatformFee(BigDecimal.ZERO);
                event.setStatus(EventStatus.ACCEPTED);
                event.setTenant(currentUser.getTenant());
            }
            else {
                // User/Customer có đăng nhập: Tính phí 10%, chờ duyệt
                calculateDefaultFee(event);
                event.setStatus(EventStatus.SCHEDULED);
            }
            event.setCreatedBy(currentUser.getUsername());
        }
        else {
            // TRƯỜNG HỢP KHÁCH VÃNG LAI (GUEST)
            calculateDefaultFee(event);
            event.setStatus(EventStatus.SCHEDULED);
            event.setCreatedBy("GUEST");
        }

        return eventMapper.toResponse(eventRepository.save(event));
    }

    // Hàm bổ trợ tính phí mặc định
    private void calculateDefaultFee(Event event) {
        if (event.getTotalAmount() != null) {
            BigDecimal commission = event.getTotalAmount().multiply(new BigDecimal("0.1"));
            event.setPlatformFee(commission);
        } else {
            event.setPlatformFee(BigDecimal.ZERO);
        }
    }

    /**
     * Cập nhật trạng thái sự kiện (Admin duyệt hoặc xác nhận hoàn thành)
     */
    @Transactional
    public EventResponse updateStatus(Long id, EventStatus status) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện"));

        event.setStatus(status);
        return eventMapper.toResponse(eventRepository.save(event));
    }

    /**
     * Lấy lịch diễn của riêng từng đội lân (Tenant) theo tháng/năm
     */
    @Transactional(readOnly = true)
    public Page<EventResponse> getTenantEvents(
            Long tenantId,
            int month,
            int year,
            Pageable pageable) {

        // Tính toán khoảng ngày của tháng đó
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        // Tận dụng Specification cũ nhưng ép tenantId cố định
        Specification<Event> spec = EventSpecification.filterEvents(
                null, null, null, tenantId, startOfMonth, endOfMonth
        );

        return eventRepository.findAll(spec, pageable)
                .map(eventMapper::toResponse);
    }

    /**
     * Lấy thống kê tóm tắt tháng cho Tenant
     */
    @Transactional(readOnly = true)
    public MonthlySummaryDto getTenantMonthlySummary(Long tenantId, int month, int year) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        Specification<Event> spec = EventSpecification.filterEvents(
                null, null, null, tenantId, startOfMonth, endOfMonth
        );

        List<Event> monthlyEvents = eventRepository.findAll(spec);

        BigDecimal totalRevenue = monthlyEvents.stream()
                .filter(e -> e.getStatus() != EventStatus.CANCELLED)
                .map(Event::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completedCount = monthlyEvents.stream()
                .filter(e -> e.getStatus() == EventStatus.COMPLETED)
                .count();

        double completionRate = monthlyEvents.isEmpty() ? 0 : (double) completedCount / monthlyEvents.size() * 100;

        return MonthlySummaryDto.builder()
                .totalEvents(monthlyEvents.size())
                .estimatedRevenue(totalRevenue)
                .completionRate(completionRate)
                .build();
    }

    @Transactional
    public void acceptEvent(Long eventId,Long tenantId){
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện"));
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tenant "));
        if(!event.getTenant().getId().equals(tenant.getId())){
            throw new RuntimeException("Bạn không phải đơn vị được ghép cho sự kiện này");
        }
        if (event.getStatus() != EventStatus.SCHEDULED) {
            throw new RuntimeException("Sự kiện không ở trạng thái SCHEDULED. Trạng thái hiện tại: " + event.getStatus());
        }

        event.setStatus(EventStatus.ACCEPTED);
        eventRepository.save(event);
    }

    @Transactional
    public void rejectEvent(Long eventId,Long tenantId){
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện"));
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tenant "));
        if(!event.getTenant().getId().equals(tenant.getId())){
            throw new RuntimeException("Bạn không phải đơn vị được ghép cho sự kiện này");
        }
        if (event.getStatus() != EventStatus.SCHEDULED) {
            throw new RuntimeException("Sự kiện không ở trạng thái SCHEDULED. Trạng thái hiện tại: " + event.getStatus());
        }

        event.setStatus(EventStatus.REJECTED);
        event.setTenant(null);
        eventRepository.save(event);
    }

    /**
     * Admin gán danh sách thành viên vào show
     */
    @Transactional
    public void assignMembersToEvent(Long eventId, List<AssignMemberRequest> requests) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện"));

        for (AssignMemberRequest req : requests) {
            User user = userRepository.findById(req.getUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thành viên ID: " + req.getUserId()));

            // Logic check trùng: Nếu đã gán rồi thì chỉ update vị trí, chưa thì tạo mới
            UserEvent userEvent = userEventRepository.findByEventIdAndUserId(eventId, user.getId())
                    .orElse(new UserEvent());

            userEvent.setEvent(event);
            userEvent.setUser(user);
            userEvent.setPosition(req.getPosition());
            userEvent.setStatus(AssignStatus.PENDING); // Luôn về PENDING để member xác nhận lại nếu có thay đổi

            userEventRepository.save(userEvent);
        }
        log.info("Admin assigned {} members to event {}", requests.size(), eventId);
    }

    // --- PHẦN 2: DÀNH CHO MEMBER (XÁC NHẬN & ĐIỂM DANH) ---

    /**
     * Member bấm xác nhận tham gia hoặc từ chối
     */
    @Transactional
    public void respondToAssignment(Long userEventId, String status, String note) {
        UserEvent userEvent = userEventRepository.findById(userEventId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi phân công"));

        userEvent.setStatus(AssignStatus.valueOf(status)); // ACCEPTED hoặc REJECTED
        userEvent.setNote(note);
        userEvent.setRespondedAt(LocalDateTime.now());

        userEventRepository.save(userEvent);
    }

    /**
     * Logic Check-in (Có thể mở rộng thêm check tọa độ GPS ở đây)
     */
    @Transactional
    public void checkIn(Long userEventId, String location) {
        UserEvent userEvent = userEventRepository.findById(userEventId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi phân công"));

        if (!"ACCEPTED".equals(userEvent.getStatus())) {
            throw new RuntimeException("Bạn chưa xác nhận tham gia show này!");
        }

        userEvent.setCheckinAt(LocalTime.now());
        userEvent.setCheckinLocation(location); // Lưu tọa độ lúc bấm
        userEventRepository.save(userEvent);
    }

    /**
     * Logic Check-out
     */
    @Transactional
    public String checkOut(Long assignmentId) {
        UserEvent assignment = userEventRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy lịch diễn"));

        if (assignment.getCheckinAt() == null) {
            throw new IllegalStateException("Bạn chưa check-in điểm diễn, không thể check-out!");
        }

        // 1. Lưu giờ check-out
        assignment.setCheckoutAt(LocalTime.now());

        // 2. QUAN TRỌNG: Chuyển trạng thái sang COMPLETED
        assignment.setStatus(AssignStatus.COMPLETED);

        userEventRepository.save(assignment);

        return "Chúc mừng bạn đã hoàn thành show diễn!";
    }

    /**
     * Lấy danh sách show diễn mà member được gán (Dùng cho Tab Lịch Diễn của Member)
     */
    @Transactional(readOnly = true)
    public List<UserEventDTO> getMyAssignedEvents(Long userId) {
        return userEventRepository.findByUserId(userId)
                .stream()
                .map(userEventMapper::toDto)
                .collect(Collectors.toList());
    }

    // --- BỔ SUNG CHO HÀM CHI TIẾT EVENT CỦA ADMIN ---

    /**
     * Lấy chi tiết show bao gồm cả mảng thành viên đi diễn (Gộp mảng)
     */
    @Transactional(readOnly = true)
    public EventWithMembersResponse getEventDetailWithMembers(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện"));

        EventResponse eventInfo = eventMapper.toResponse(event);

        List<UserEventDTO> members = userEventRepository.findByEventId(eventId)
                .stream()
                .map(userEventMapper::toDto)
                .collect(Collectors.toList());

        return new EventWithMembersResponse(eventInfo, members);
    }

    @Transactional
    public EventResponse updateConcentrateInfo(Long eventId, UpdateConcentrate updateDto) {
        // 1. Tìm Entity từ DB
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy show diễn"));

        // 2. Dùng Mapper để đổ dữ liệu từ DTO vào Entity đã tìm thấy
        // Mapper sẽ tự động cập nhật concentrateTime và concentrateLocation
        eventMapper.updateConcentrate(updateDto, event);

        // 3. Lưu xuống DB
        Event savedEvent = eventRepository.save(event);

        // 4. Trả về DTO sau khi update
        return eventMapper.toResponse(savedEvent);
    }

    @Transactional
    public String concentrateCheckIn(Long assignmentId, String location) {
        // 1. Tìm bản ghi Assignment
        UserEvent assignment = userEventRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy lịch diễn"));

        // 2. Validate trạng thái (Chỉ cho phép tập trung khi đã ACCEPTED)
        if (assignment.getStatus() != AssignStatus.ACCEPTED) {
            throw new IllegalStateException("Bạn phải chấp nhận show diễn trước khi check-in tập trung.");
        }

        // 3. Kiểm tra xem đã tập trung chưa (tránh bấm 2 lần)
        if (assignment.getConcentrateAt() != null) {
            throw new IllegalStateException("Bạn đã xác nhận có mặt tập trung rồi!");
        }

        // 4. Lưu dữ liệu thực tế
        assignment.setConcentrateAt(LocalTime.now());
        assignment.setConcentrateLocationActual(location); // Lưu GPS thực tế lúc đó

        userEventRepository.save(assignment);

        return "Xác nhận có mặt tập trung thành công!";
    }
}