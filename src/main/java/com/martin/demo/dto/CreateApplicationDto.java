package com.martin.demo.dto;

import java.math.BigDecimal;

public record CreateApplicationDto(
        String type,           // ApplicationType enum name
        String description,
        BigDecimal amount
) {}
