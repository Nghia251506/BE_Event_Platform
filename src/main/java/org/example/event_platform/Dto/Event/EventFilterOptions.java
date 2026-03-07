package org.example.event_platform.Dto.Event;

import lombok.*;
import org.example.event_platform.Dto.Tenant.TenantSimpleResponse;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class EventFilterOptions {
    private List<TenantSimpleResponse> tenants; // List {id, name} của các đội lân
    private Map<String, String> eventTypes;     // List các loại sự kiện (Key: MID_AUTUMN, Value: Tết Trung Thu)
    private List<String> statuses;              // List các trạng thái
}