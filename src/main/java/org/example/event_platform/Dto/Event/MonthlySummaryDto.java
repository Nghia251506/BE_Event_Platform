package org.example.event_platform.Dto.Event;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
public class MonthlySummaryDto {
    private int totalEvents;
    private BigDecimal estimatedRevenue;
    private double completionRate;
}
