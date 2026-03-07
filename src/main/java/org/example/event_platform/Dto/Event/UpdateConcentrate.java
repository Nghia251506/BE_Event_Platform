package org.example.event_platform.Dto.Event;

import lombok.Data;

import java.time.LocalTime;

@Data
public class UpdateConcentrate {
    private LocalTime concentrateTime;
    private String concentrateLocation;
}
