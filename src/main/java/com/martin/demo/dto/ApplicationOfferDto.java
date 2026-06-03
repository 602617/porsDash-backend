package com.martin.demo.dto;

import com.martin.demo.model.ApplicationOffer;

import java.math.BigDecimal;
import java.time.Instant;

public record ApplicationOfferDto(
        Long id,
        BigDecimal amount,
        String description,
        String offeredByUsername,
        boolean counterOffer,
        Instant createdAt
) {
    public static ApplicationOfferDto from(ApplicationOffer o) {
        return new ApplicationOfferDto(
                o.getId(),
                o.getAmount(),
                o.getDescription(),
                o.getOfferedBy().getUsername(),
                o.isCounterOffer(),
                o.getCreatedAt()
        );
    }
}
