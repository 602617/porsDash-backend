package com.martin.demo.dto;

import com.martin.demo.model.LoanPayment;

import java.math.BigDecimal;
import java.time.Instant;

public record LoanPaymentDto(Long id, BigDecimal amount, Instant paidAt, String note, String createdBy) {
    public static LoanPaymentDto from(LoanPayment p) {
        return new LoanPaymentDto(
                p.getId(),
                p.getAmount(),
                p.getPaidAt(),
                p.getNote(),
                p.getCreatedBy().getUsername()
        );
    }
}
