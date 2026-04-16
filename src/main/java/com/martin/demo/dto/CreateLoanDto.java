package com.martin.demo.dto;

import java.math.BigDecimal;

public record CreateLoanDto(
        String otherUsername,   // the other party (borrower or lender)
        String myRole,          // "BORROWER" or "LENDER"
        BigDecimal amount,
        String title
) {}
