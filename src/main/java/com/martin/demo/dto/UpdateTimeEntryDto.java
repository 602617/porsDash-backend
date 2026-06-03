package com.martin.demo.dto;

import java.time.Instant;

public record UpdateTimeEntryDto(
        Instant startTime,
        Instant endTime,
        String note
) {}
