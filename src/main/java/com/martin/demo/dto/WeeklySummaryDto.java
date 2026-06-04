package com.martin.demo.dto;

import java.time.LocalDate;
import java.util.List;

public record WeeklySummaryDto(
        LocalDate weekStart,
        LocalDate weekEnd,
        long totalMinutes,
        long overtimeMinutes,
        List<DaySummaryDto> days
) {
    public record DaySummaryDto(
            LocalDate date,
            String dayName,
            long totalMinutes,
            long overtimeMinutes,
            int entryCount
    ) {}
}
