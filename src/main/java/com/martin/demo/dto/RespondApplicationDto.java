package com.martin.demo.dto;

import java.math.BigDecimal;

public record RespondApplicationDto(
        String action,          // "ACCEPT", "DECLINE", or "COUNTER"
        BigDecimal amount,      // required if action is COUNTER
        String description      // optional new description for COUNTER
) {}
