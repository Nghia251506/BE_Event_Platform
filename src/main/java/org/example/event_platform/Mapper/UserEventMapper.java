package org.example.event_platform.Mapper;
import org.example.event_platform.Dto.Event.UserEventDTO;
import org.example.event_platform.Entity.UserEvent;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring") // Để Spring quản lý như một Bean (@Component)
public abstract class UserEventMapper {

    // Ánh xạ từ Entity sang DTO
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "fullName")
    @Mapping(target = "eventName", source = "event.name")
    @Mapping(target = "eventDate", source = "event.eventDate")
    @Mapping(target = "location", source = "event.location")
    @Mapping(target = "startTime", source = "event.startTime")
    @Mapping(target = "endTime", source = "event.endTime")
    @Mapping(target = "note", source = "event.description")
    @Mapping(target = "concentrateTime", source = "event.concentrateTime")
    @Mapping(target = "concentrateLocation", source = "event.concentrateLocation")
    public abstract UserEventDTO toDto(UserEvent entity);
    // Tự động xử lý danh sách đồng đội sau khi ánh xạ xong
    @AfterMapping
    protected void fillTeammates(UserEvent entity, @MappingTarget UserEventDTO dto) {
        // Kiểm tra event và danh sách assignedMembers ông đã đặt tên
        if (entity.getEvent() != null && entity.getEvent().getAssignedMembers() != null) {
            dto.setTeammates(entity.getEvent().getAssignedMembers().stream()
                    // Loại bỏ chính mình (người đang sở hữu cái DTO này) ra khỏi danh sách đồng đội
                    .filter(ue -> !ue.getUser().getId().equals(entity.getUser().getId()))
                    .map(ue -> {
                        UserEventDTO.TeammateDTO tDto = new UserEventDTO.TeammateDTO();
                        tDto.setFullName(ue.getUser().getFullName());
                        tDto.setPosition(ue.getPosition());
                        tDto.setStatus(String.valueOf(ue.getStatus()));
                        return tDto;
                    })
                    .collect(Collectors.toList()));
        }
    }

    // Ánh xạ ngược lại từ DTO sang Entity (thường dùng khi update)
    @Mapping(target = "user", ignore = true) // User thường được set riêng trong Service
    @Mapping(target = "event", ignore = true)
    public abstract UserEvent toEntity(UserEventDTO dto);
}
