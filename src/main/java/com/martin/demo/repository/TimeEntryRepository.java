package com.martin.demo.repository;

import com.martin.demo.model.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {

    List<TimeEntry> findByUserUsernameOrderByStartTimeDesc(String username);

    Optional<TimeEntry> findByUserUsernameAndEndTimeIsNull(String username);

    List<TimeEntry> findByUserUsernameAndStartTimeBetween(String username, Instant from, Instant to);
}
