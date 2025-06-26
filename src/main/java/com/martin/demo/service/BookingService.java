package com.martin.demo.service;

import com.martin.demo.auth.AppUser;
import com.martin.demo.model.Booking;
import com.martin.demo.model.BookingStatus;
import com.martin.demo.model.ItemUnavailability;
import com.martin.demo.model.Items;
import com.martin.demo.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository repo;
    private final ItemRepository itemRepo;
    private final AppUserRepository userRepo;
    private final ItemUnavailabilityRepository itemUnavailabilityRepository;

    public BookingService(BookingRepository repo,
                          ItemRepository itemRepo,
                          AppUserRepository userRepo, ItemUnavailabilityRepository itemUnavailabilityRepository) {
        this.repo     = repo;
        this.itemRepo = itemRepo;
        this.userRepo = userRepo;
        this.itemUnavailabilityRepository = itemUnavailabilityRepository;
    }

    public List<Booking> findBookingsForItem(Long itemID) {
        itemRepo.findById(itemID).orElseThrow(() -> new EntityNotFoundException("Item not found " + itemID));

        return repo.findByItemId(itemID);
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
        var blocked = itemUnavailabilityRepository.findOverlapping(itemId, start, end);
        if (!blocked.isEmpty()) {
            throw new IllegalStateException("Tidsrommet er blokkert av eier");
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
