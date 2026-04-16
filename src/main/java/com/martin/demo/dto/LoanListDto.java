package com.martin.demo.dto;

import com.martin.demo.model.Loan;

import java.math.BigDecimal;
import java.time.Instant;

public record LoanListDto(
        Long id,
        String title,
        String borrowerUsername,
        String lenderUsername,
        BigDecimal principalAmount,
        Instant createdAt
) {
    public static LoanListDto from(Loan loan) {
        return new LoanListDto(
                loan.getId(),
                loan.getTitle(),
                loan.getBorrower().getUsername(),
                loan.getLender().getUsername(),
                loan.getPrincipalAmount(),
                loan.getCreatedAt()
        );
    }
}
