package com.martin.demo.dto;

import com.martin.demo.model.TimeEntry;

import java.time.Instant;

public record TimeEntryDto(
        Long id,
        Instant startTime,
        Instant endTime,
        Long totalMinutes,
        Long overtimeMinutes,
        String note,
        boolean running
) {
    public static TimeEntryDto from(TimeEntry e) {
        return new TimeEntryDto(
                e.getId(),
                e.getStartTime(),
                e.getEndTime(),
                e.getTotalMinutes(),
                e.getOvertimeMinutes(),
                e.getNote(),
                e.getEndTime() == null
        );
    }
}
