package org.example.event_platform.Mapper;
import org.example.event_platform.Dto.Event.UserEventDTO;
import org.example.event_platform.Entity.AssignStatus;
import org.example.event_platform.Entity.UserEvent;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class UserEventMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "fullName")
    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventName", source = "event.name")
    @Mapping(target = "eventDate", source = "event.eventDate")
    @Mapping(target = "location", source = "event.location")
    @Mapping(target = "startTime", source = "event.startTime")
    @Mapping(target = "endTime", source = "event.endTime")
    @Mapping(target = "concentrateTime", source = "event.concentrateTime")
    @Mapping(target = "concentrateLocation", source = "event.concentrateLocation")
    // Note này là description của Show để anh em đọc lưu ý
    @Mapping(target = "note", source = "event.description")
    // Status của cá nhân: PENDING, ACCEPTED...
    @Mapping(target = "status", expression = "java(formatAssignStatus(entity.getStatus()))")
    public abstract UserEventDTO toDto(UserEvent entity);

    @AfterMapping
    protected void fillTeammates(UserEvent entity, @MappingTarget UserEventDTO dto) {
        if (entity.getEvent() != null && entity.getEvent().getAssignedMembers() != null) {
            dto.setTeammates(entity.getEvent().getAssignedMembers().stream()
                    .filter(ue -> !ue.getUser().getId().equals(entity.getUser().getId()))
                    .map(ue -> new UserEventDTO.TeammateDTO(
                            ue.getUser().getFullName(),
                            ue.getPosition(),
                            formatAssignStatus(ue.getStatus())
                    ))
                    .collect(Collectors.toList()));
        }
    }

    // Helper: Tiếng Việt hóa trạng thái của Anh Em
    protected String formatAssignStatus(AssignStatus status) {
        if (status == null) return "Chờ xác nhận";
        return switch (status) {
            case PENDING -> "Đang mời";
            case ACCEPTED -> "Sẵn sàng";
            case REJECTED -> "Từ chối";
            case COMPLETED -> "Đã diễn xong"; // Chính là FINISHED như ae mình bàn
            case CHECKED_IN -> "Đã checkin diễn";
            case CHECKED_OUT -> "Đã checkout diễn";
        };
    }

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "event", ignore = true)
    public abstract UserEvent toEntity(UserEventDTO dto);
}
