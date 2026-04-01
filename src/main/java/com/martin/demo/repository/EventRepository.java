package com.martin.demo.repository;

import com.martin.demo.model.Event;
import com.martin.demo.model.EventAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event,Long> {

    @org.springframework.data.jpa.repository.Query("""
        SELECT DISTINCT e FROM Event e
        LEFT JOIN e.attendees a
        WHERE e.createdBy.username = :username
           OR a.user.username = :username
        ORDER BY e.startTime ASC
    """)
    List<Event> findVisibleToUser(@org.springframework.data.repository.query.Param("username") String username);
}
