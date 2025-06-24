package com.martin.demo.repository;

import com.martin.demo.model.ItemAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemAvailabilityRepository extends JpaRepository<ItemAvailability, Long> {
    List<ItemAvailability> findByItemIdOrderByStartTime(Long itemId);
    @Query("""
    SELECT a FROM ItemAvailability a
     WHERE a.item.id = :itemId
       AND a.startTime < :end
       AND a.endTime   > :start
  """)
    List<ItemAvailability> findOverlapping(Long itemId, LocalDateTime start, LocalDateTime end);
}
