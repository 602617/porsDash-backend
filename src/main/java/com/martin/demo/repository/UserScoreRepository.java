package com.martin.demo.repository;

import com.martin.demo.model.UserScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserScoreRepository extends JpaRepository<UserScore, Long> {
    Optional<UserScore> findByUserUsername(String username);
}
