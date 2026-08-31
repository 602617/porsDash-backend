package com.martin.demo.service;

import com.martin.demo.auth.AppUser;
import com.martin.demo.model.ScoreLog;
import com.martin.demo.model.ScoringAction;
import com.martin.demo.model.UserScore;
import com.martin.demo.repository.AppUserRepository;
import com.martin.demo.repository.ScoreLogRepository;
import com.martin.demo.repository.UserScoreRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScoreService {

    private final UserScoreRepository scores;
    private final ScoreLogRepository logs;
    private final AppUserRepository users;

    public ScoreService(UserScoreRepository scores,
                        ScoreLogRepository logs,
                        AppUserRepository users) {
        this.scores = scores;
        this.logs = logs;
        this.users = users;
    }

    public void award(String username, ScoringAction action) {
        int points = action.getPoints();
        if (points <= 0) return;

        AppUser user = users.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        UserScore score = scores.findByUserUsername(username)
                .orElseGet(() -> {
                    UserScore s = new UserScore();
                    s.setUser(user);
                    s.setTotalPoints(0);
                    return s;
                });

        score.setTotalPoints(score.getTotalPoints() + points);
        score.setUpdatedAt(LocalDateTime.now());
        scores.save(score);

        ScoreLog log = new ScoreLog();
        log.setUser(user);
        log.setAction(action);
        log.setPoints(points);
        logs.save(log);
    }

    public int getScore(String username) {
        return scores.findByUserUsername(username)
                .map(UserScore::getTotalPoints)
                .orElse(0);
    }

    public List<ScoreLog> getHistory(String username) {
        return logs.findByUserUsernameOrderByCreatedAtDesc(username);
    }
}
