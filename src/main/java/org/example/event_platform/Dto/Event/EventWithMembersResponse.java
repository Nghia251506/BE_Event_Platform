package org.example.event_platform.Dto.Event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventWithMembersResponse {

    // Thông tin cơ bản của Event (Tận dụng lại DTO cũ của ông)
    private EventResponse eventInfo;

    // Danh sách thành viên đã được gán vào show này
    private List<UserEventDTO> members;
}