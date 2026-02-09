package com.martin.demo.service;

import com.martin.demo.auth.AppUser;
import com.martin.demo.model.Booking;
import com.martin.demo.model.BookingStatus;
import com.martin.demo.model.ItemUnavailability;
import com.martin.demo.model.Items;
import com.martin.demo.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository repo;
    private final ItemRepository itemRepo;
    private final AppUserRepository userRepo;
    private final ItemUnavailabilityRepository itemUnavailabilityRepository;
    private final NotificationService notificationService;

    public BookingService(BookingRepository repo,
                          ItemRepository itemRepo,
                          AppUserRepository userRepo, ItemUnavailabilityRepository itemUnavailabilityRepository, NotificationService notificationService) {
        this.repo     = repo;
        this.itemRepo = itemRepo;
        this.userRepo = userRepo;
        this.itemUnavailabilityRepository = itemUnavailabilityRepository;
        this.notificationService = notificationService;
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

        notificationService.notifyOwner(
                item.getUser().getId(),
                usr.getUsername() + " har booket " + item.getName(),
                "/items/" + item.getId()
        );
        return repo.save(b);
    }

    public void cancelBooking(Long itemId, Long bookingId, String username) {

        // 1) Ensure item exists
        Items item = itemRepo.findById(itemId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Item ikke funnet " + itemId)
                );

        // 2) Find booking
        Booking booking = repo.findById(bookingId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Booking ikke funnet " + bookingId)
                );

        // 3) Booking must belong to item
        if (booking.getItem() == null
                || booking.getItem().getId() == null
                || !booking.getItem().getId().equals(itemId)) {
            throw new EntityNotFoundException(
                    "Booking tilhører ikke item " + itemId
            );
        }

        // 4) Permission: owner OR booker
        String ownerUsername =
                item.getUser() != null ? item.getUser().getUsername() : null;

        String bookerUsername =
                booking.getUser() != null ? booking.getUser().getUsername() : null;

        boolean isOwner = ownerUsername != null && ownerUsername.equals(username);
        boolean isBooker = bookerUsername != null && bookerUsername.equals(username);

        if (!isOwner && !isBooker) {
            throw new AccessDeniedException(
                    "Kun eier eller den som opprettet bookingen kan kansellere"
            );
        }

        // 5) Prevent double-cancel
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return; // idempotent
        }

        // 6) Cancel (soft delete)
        booking.setStatus(BookingStatus.CANCELLED);
        repo.save(booking);

        // 7) Optional notifications
        if (isOwner && !isBooker) {
            notificationService.notifyUser(
                    booking.getUser().getId(),
                    "Bookingen for " + item.getName() + " ble kansellert av eier",
                    "/bookings/" + booking.getId()
            );
        }

        if (isBooker && !isOwner) {
            notificationService.notifyOwner(
                    item.getUser().getId(),
                    "Bookingen for " + item.getName() + " ble kansellert av bruker",
                    "/items/" + item.getId()
            );
        }
    }
}
