package com.martin.demo.service;

import com.martin.demo.auth.AppUser;
import com.martin.demo.model.Booking;
import com.martin.demo.model.BookingStatus;
import com.martin.demo.model.Items;
import com.martin.demo.pushnotifications.notifications.NotificationService;
import com.martin.demo.repository.AppUserRepository;
import com.martin.demo.repository.BookingRepository;
import com.martin.demo.repository.ItemRepository;
import com.martin.demo.repository.ItemUnavailabilityRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
                          AppUserRepository userRepo,
                          ItemUnavailabilityRepository itemUnavailabilityRepository,
                          NotificationService notificationService) {
        this.repo = repo;
        this.itemRepo = itemRepo;
        this.userRepo = userRepo;
        this.itemUnavailabilityRepository = itemUnavailabilityRepository;
        this.notificationService = notificationService;
    }

    public List<Booking> findBookingsForUser(String username) {
        return repo.findByUserUsernameOrderByStartTimeDesc(username);
    }

    public List<Booking> findBookingsForItem(Long itemID) {
        ensureItemExists(itemID);
        return repo.findByItemId(itemID);
    }

    public Booking findBooking(Long itemId, Long bookingId) {
        ensureItemExists(itemId);
        return ensureBookingBelongsToItem(itemId, bookingId);
    }

    public Booking createBooking(Long itemId, Long userId,
                                 LocalDateTime start,
                                 LocalDateTime end) {
        ensureTimeRangeValid(start, end);

        Items item = ensureItemExists(itemId);
        AppUser usr = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User ikke funnet"));

        ensureNotBlocked(itemId, start, end);

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setUser(usr);
        booking.setStartTime(start);
        booking.setEndTime(end);
        booking.setStatus(BookingStatus.PENDING);

        Booking savedBooking = repo.save(booking);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM HH:mm");
        notificationService.notifyOwner(
                item.getUser().getId(),
                usr.getUsername() + " har booket " + item.getName()
                        + " (" + start.format(fmt) + " - " + end.format(fmt) + ")",
                "/items/" + item.getId() + "/bookings/" + savedBooking.getId()
        );

        return savedBooking;
    }

    public Booking updateBooking(Long itemId,
                                 Long bookingId,
                                 LocalDateTime start,
                                 LocalDateTime end,
                                 String username) {
        ensureTimeRangeValid(start, end);

        Items item = ensureItemExists(itemId);
        Booking booking = ensureBookingBelongsToItem(itemId, bookingId);

        ensureOwner(item, username, "Kun eier kan endre booking");

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.DECLINED) {
            throw new IllegalStateException("Kan ikke endre en booking som er kansellert eller avvist");
        }

        ensureNotBlocked(itemId, start, end);
        ensureNoConfirmedConflict(itemId, start, end, booking.getId());

        booking.setStartTime(start);
        booking.setEndTime(end);
        booking.setUpdatedAt(LocalDateTime.now());

        Booking saved = repo.save(booking);

        if (booking.getUser() != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM HH:mm");
            notificationService.notifyUser(
                    booking.getUser().getId(),
                    "Bookingen din for " + item.getName() + " ble oppdatert til "
                            + saved.getStartTime().format(fmt) + " - " + saved.getEndTime().format(fmt),
                    "/items/" + item.getId() + "/bookings/" + booking.getId()
            );
        }

        return saved;
    }

    public Booking approveBooking(Long itemId, Long bookingId, String username) {
        Items item = ensureItemExists(itemId);
        Booking booking = ensureBookingBelongsToItem(itemId, bookingId);

        ensureOwner(item, username, "Kun eier kan godkjenne booking");

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Booking er ikke i PENDING status");
        }

        ensureNotBlocked(itemId, booking.getStartTime(), booking.getEndTime());
        ensureNoConfirmedConflict(itemId, booking.getStartTime(), booking.getEndTime(), booking.getId());

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setUpdatedAt(LocalDateTime.now());
        Booking saved = repo.save(booking);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM HH:mm");
        notificationService.notifyUser(
                booking.getUser().getId(),
                "Bookingen din for " + item.getName()
                        + " (" + booking.getStartTime().format(fmt) + " - " + booking.getEndTime().format(fmt) + ") er godkjent!",
                "/items/" + item.getId() + "/bookings/" + booking.getId()
        );

        return saved;
    }

    public Booking declineBooking(Long itemId, Long bookingId, String username) {
        Items item = ensureItemExists(itemId);
        Booking booking = ensureBookingBelongsToItem(itemId, bookingId);

        ensureOwner(item, username, "Kun eier kan avslå booking");

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Booking er ikke i PENDING status");
        }

        booking.setStatus(BookingStatus.DECLINED);
        booking.setUpdatedAt(LocalDateTime.now());
        Booking saved = repo.save(booking);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM HH:mm");
        notificationService.notifyUser(
                booking.getUser().getId(),
                "Bookingen din for " + item.getName()
                        + " (" + booking.getStartTime().format(fmt) + " - " + booking.getEndTime().format(fmt) + ") ble avslått.",
                "/items/" + item.getId() + "/bookings/" + booking.getId()
        );

        return saved;
    }

    public void cancelBooking(Long itemId, Long bookingId, String username) {
        Items item = ensureItemExists(itemId);
        Booking booking = ensureBookingBelongsToItem(itemId, bookingId);

        String ownerUsername = item.getUser() != null ? item.getUser().getUsername() : null;
        String bookerUsername = booking.getUser() != null ? booking.getUser().getUsername() : null;

        boolean isOwner = ownerUsername != null && ownerUsername.equals(username);
        boolean isBooker = bookerUsername != null && bookerUsername.equals(username);

        if (!isOwner && !isBooker) {
            throw new AccessDeniedException(
                    "Kun eier eller den som opprettet bookingen kan kansellere"
            );
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return;
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());
        repo.save(booking);

        if (isOwner && !isBooker) {
            notificationService.notifyUser(
                    booking.getUser().getId(),
                    "Bookingen for " + item.getName() + " ble kansellert av eier",
                    "/items/" + item.getId() + "/bookings/" + booking.getId()
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

    private Items ensureItemExists(Long itemId) {
        return itemRepo.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item ikke funnet " + itemId));
    }

    private Booking ensureBookingBelongsToItem(Long itemId, Long bookingId) {
        Booking booking = repo.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking ikke funnet " + bookingId));

        if (!booking.getItem().getId().equals(itemId)) {
            throw new EntityNotFoundException("Booking tilhorer ikke item " + itemId);
        }

        return booking;
    }

    private void ensureOwner(Items item, String username, String message) {
        String ownerUsername = item.getUser() != null ? item.getUser().getUsername() : null;
        if (ownerUsername == null || !ownerUsername.equals(username)) {
            throw new AccessDeniedException(message);
        }
    }

    private void ensureTimeRangeValid(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalStateException("Start og sluttid ma være satt");
        }
        if (!end.isAfter(start)) {
            throw new IllegalStateException("Sluttid ma være etter starttid");
        }
    }

    private void ensureNotBlocked(Long itemId, LocalDateTime start, LocalDateTime end) {
        var blocked = itemUnavailabilityRepository.findOverlapping(itemId, start, end);
        if (!blocked.isEmpty()) {
            throw new IllegalStateException("Tidsrommet er blokkert av eier");
        }
    }

    private void ensureNoConfirmedConflict(Long itemId,
                                           LocalDateTime start,
                                           LocalDateTime end,
                                           Long currentBookingId) {
        var conflicts = repo.findConflictingExcludingBooking(itemId, start, end, currentBookingId);
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("Tidsrommet overlapper en annen godkjent booking");
        }
    }
}
