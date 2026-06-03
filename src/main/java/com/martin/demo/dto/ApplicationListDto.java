package com.martin.demo.dto;

import com.martin.demo.model.Application;
import com.martin.demo.model.ApplicationOffer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ApplicationListDto(
        Long id,
        String type,
        String status,
        String senderUsername,
        String respondedByUsername,
        BigDecimal currentAmount,
        String currentDescription,
        Instant updatedAt
) {
    public static ApplicationListDto from(Application a) {
        List<ApplicationOffer> offerList = a.getOffers();
        ApplicationOffer latest = offerList.isEmpty() ? null : offerList.get(offerList.size() - 1);

        return new ApplicationListDto(
                a.getId(),
                a.getType().name(),
                a.getStatus().name(),
                a.getSender().getUsername(),
                a.getRespondedBy() != null ? a.getRespondedBy().getUsername() : null,
                latest != null ? latest.getAmount() : null,
                latest != null ? latest.getDescription() : null,
                a.getUpdatedAt()
        );
    }
}
