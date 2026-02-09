package com.martin.demo.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record UpdatePaymentDto(BigDecimal amount, Instant paidAt, String note) {
}
