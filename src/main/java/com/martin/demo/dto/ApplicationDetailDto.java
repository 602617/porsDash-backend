package com.martin.demo.dto;

import com.martin.demo.model.Application;

import java.time.Instant;
import java.util.List;

public record ApplicationDetailDto(
        Long id,
        String type,
        String status,
        String senderUsername,
        String respondedByUsername,
        Instant createdAt,
        Instant updatedAt,
        List<ApplicationOfferDto> offers,
        ApplicationOfferDto currentOffer
) {
    public static ApplicationDetailDto from(Application a) {
        List<ApplicationOfferDto> offerDtos = a.getOffers().stream()
                .map(ApplicationOfferDto::from)
                .toList();

        ApplicationOfferDto current = offerDtos.isEmpty() ? null : offerDtos.get(offerDtos.size() - 1);

        return new ApplicationDetailDto(
                a.getId(),
                a.getType().name(),
                a.getStatus().name(),
                a.getSender().getUsername(),
                a.getRespondedBy() != null ? a.getRespondedBy().getUsername() : null,
                a.getCreatedAt(),
                a.getUpdatedAt(),
                offerDtos,
                current
        );
    }
}
