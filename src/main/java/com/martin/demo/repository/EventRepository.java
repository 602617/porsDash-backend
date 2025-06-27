package com.martin.demo.repository;

import com.martin.demo.model.Event;
import com.martin.demo.model.EventAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event,Long> {
}
