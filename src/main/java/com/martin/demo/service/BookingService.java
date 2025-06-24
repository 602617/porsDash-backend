package com.martin.demo.service;

import com.martin.demo.auth.AppUser;
import com.martin.demo.model.Booking;
import com.martin.demo.model.BookingStatus;
import com.martin.demo.model.Items;
import com.martin.demo.repository.AppUserRepository;
import com.martin.demo.repository.BookingRepository;
import com.martin.demo.repository.ItemAvailabilityRepository;
import com.martin.demo.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BookingService {

    private final BookingRepository repo;
    private final ItemRepository itemRepo;
    private final AppUserRepository userRepo;
    private final ItemAvailabilityRepository availabilityRepo;

    public BookingService(BookingRepository repo,
                          ItemRepository itemRepo,
                          AppUserRepository userRepo, ItemAvailabilityRepository availabilityRepo) {
        this.repo     = repo;
        this.itemRepo = itemRepo;
        this.userRepo = userRepo;
        this.availabilityRepo = availabilityRepo;
    }

    public Booking createBooking(Long itemId, Long userId,
                                 LocalDateTime start,
                                 LocalDateTime end) {
        // 1. Hent entiteter
        Items item   = itemRepo.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item ikke funnet"));
        AppUser usr = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User ikke funnet"));

        // 2. Sjekk overlapp
        var conflicts = repo.findConflicting(itemId, start, end);
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("Tidsrommet er allerede booket");
        }

        boolean inside = availabilityRepo.findByItemIdOrderByStartTime(itemId).stream()
                .anyMatch(a -> !start.isBefore(a.getStartTime()) && !end.isAfter(a.getEndTime()));
        if (!inside) {
            throw new IllegalStateException("Valgt tidsrom er ikke tilgjengelig for booking");
        }

        // 3. Lag og lagre booking
        Booking b = new Booking();
        b.setItem(item);
        b.setUser(usr);
        b.setStartTime(start);
        b.setEndTime(end);
        b.setStatus(BookingStatus.PENDING);

        return repo.save(b);
    }
}
