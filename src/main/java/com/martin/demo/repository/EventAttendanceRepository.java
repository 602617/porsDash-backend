package com.martin.demo.repository;

import com.martin.demo.model.EventAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventAttendanceRepository extends JpaRepository<EventAttendance, Long> {
    Optional<EventAttendance> findByEventIdAndUserId(Long eventId, Long userId);
    List<EventAttendance> findByEventId(Long eventId);
}
