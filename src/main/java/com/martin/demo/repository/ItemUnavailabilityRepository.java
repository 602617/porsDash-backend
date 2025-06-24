package com.martin.demo.repository;

import com.martin.demo.model.ItemUnavailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemUnavailabilityRepository extends JpaRepository<ItemUnavailability, Long> {
    List<ItemUnavailability> findByItemId(Long itemId);
    @Query("""
    SELECT b FROM ItemUnavailability b
     WHERE b.item.id = :itemId
       AND b.startTime < :end
       AND b.endTime   > :start
  """)
    List<ItemUnavailability> findOverlapping(Long itemId,
                                             LocalDateTime start,
                                             LocalDateTime end);
}
