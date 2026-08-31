package com.martin.demo.repository;

import com.martin.demo.model.ScoreLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScoreLogRepository extends JpaRepository<ScoreLog, Long> {
    List<ScoreLog> findByUserUsernameOrderByCreatedAtDesc(String username);
}
