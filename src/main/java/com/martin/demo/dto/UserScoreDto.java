package com.martin.demo.dto;

import java.util.List;

public record UserScoreDto(
        String username,
        int totalPoints,
        List<ScoreLogDto> history
) {
}
