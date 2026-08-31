package com.martin.demo.dto;

import com.martin.demo.model.ScoreLog;

import java.time.LocalDateTime;

public record ScoreLogDto(
        Long id,
        String action,
        int points,
        LocalDateTime createdAt
) {
    public static ScoreLogDto from(ScoreLog log) {
        return new ScoreLogDto(
                log.getId(),
                log.getAction().name(),
                log.getPoints(),
                log.getCreatedAt()
        );
    }
}
