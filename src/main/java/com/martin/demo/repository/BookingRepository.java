package com.martin.demo.repository;

import com.martin.demo.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("""
    SELECT b FROM Booking b
     WHERE b.item.id = :itemId
       AND b.status = 'CONFIRMED'
       AND b.startTime < :end
       AND b.endTime   > :start
  """)
    List<Booking> findConflicting(
            @Param("itemId") Long itemId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("""
    SELECT b FROM Booking b
     WHERE b.item.id = :itemId
       AND b.status = 'CONFIRMED'
       AND b.id <> :bookingId
       AND b.startTime < :end
       AND b.endTime   > :start
  """)
    List<Booking> findConflictingExcludingBooking(
            @Param("itemId") Long itemId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("bookingId") Long bookingId);

    List<Booking> findByItemId(Long itemId);

    List<Booking> findByUserUsernameOrderByStartTimeDesc(String username);
}
