package com.martin.demo.dto;

public record TimeEntrySummaryDto(
        long totalMinutes,
        long overtimeMinutes,
        int entryCount
) {}
