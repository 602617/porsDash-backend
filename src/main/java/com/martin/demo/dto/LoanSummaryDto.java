package com.martin.demo.dto;

import com.martin.demo.model.Loan;
import com.martin.demo.model.LoanPayment;

import java.math.BigDecimal;
import java.util.List;

public record LoanSummaryDto(
        Long loanId,
        BigDecimal principalAmount,
        BigDecimal sumPaid,
        BigDecimal sumLeft,
        List<LoanPaymentDto> history
) {
    public static LoanSummaryDto from(Loan loan, BigDecimal sumPaid, BigDecimal sumLeft, List<LoanPayment> history) {
        return new LoanSummaryDto(
                loan.getId(),
                loan.getPrincipalAmount(),
                sumPaid,
                sumLeft,
                history.stream().map(LoanPaymentDto::from).toList()
        );
    }
}

