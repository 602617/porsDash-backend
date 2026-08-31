package com.martin.demo.Controller;

import com.martin.demo.dto.ScoreLogDto;
import com.martin.demo.dto.UserScoreDto;
import com.martin.demo.service.ScoreService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/score")
public class ScoreController {

    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @GetMapping
    public UserScoreDto getMyScore(Principal principal) {
        String username = principal.getName();
        int total = scoreService.getScore(username);
        List<ScoreLogDto> history = scoreService.getHistory(username)
                .stream()
                .map(ScoreLogDto::from)
                .toList();
        return new UserScoreDto(username, total, history);
    }
}
