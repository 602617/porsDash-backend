package com.martin.demo.Controller;

import com.martin.demo.auth.AppUser;
import com.martin.demo.dto.BookingDto;
import com.martin.demo.dto.BookingRequest;
import com.martin.demo.model.Booking;
import com.martin.demo.repository.AppUserRepository;
import com.martin.demo.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/items/{itemId}/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final AppUserRepository appUserRepository;


    public BookingController(BookingService bookingService, AppUserRepository appUserRepository) {
        this.bookingService = bookingService;
        this.appUserRepository = appUserRepository;
    }

    private BookingDto toDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getItem().getId(),
                booking.getUser().getId(),
                booking.getUser().getUsername(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getStatus()
        );
    }

    @GetMapping
    public List<BookingDto> listBookings(@PathVariable Long itemId) {
        return bookingService.findBookingsForItem(itemId).stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(
            @PathVariable Long itemId,
            @PathVariable Long bookingId) {
        return toDto(bookingService.findBooking(itemId, bookingId));
    }

    @PostMapping("/{bookingId}/approve")
    public BookingDto approveBooking(
            @PathVariable Long itemId,
            @PathVariable Long bookingId,
            Principal principal) {
        return toDto(bookingService.approveBooking(itemId, bookingId, principal.getName()));
    }

    @PostMapping
    public ResponseEntity<Booking> bookItem(
            @PathVariable Long itemId,
            @RequestBody BookingRequest req,
            Principal principal) {
        AppUser currentUser = appUserRepository.findByUsername(principal.getName()).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Booking saved = bookingService.createBooking(
                itemId,
                currentUser.getId(),
                req.getStartTime(),
                req.getEndTime()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @PutMapping("/{bookingId}")
    public BookingDto updateBooking(
            @PathVariable Long itemId,
            @PathVariable Long bookingId,
            @RequestBody BookingRequest req,
            Principal principal
    ) {
        Booking updated = bookingService.updateBooking(
                itemId,
                bookingId,
                req.getStartTime(),
                req.getEndTime(),
                principal.getName()
        );
        return toDto(updated);
    }

    @PostMapping("/{bookingId}/decline")
    public BookingDto declineBooking(
            @PathVariable Long itemId,
            @PathVariable Long bookingId,
            Principal principal) {
        return toDto(bookingService.declineBooking(itemId, bookingId, principal.getName()));
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long itemId,
            @PathVariable Long bookingId,
            Principal principal
    ) {
        bookingService.cancelBooking(itemId, bookingId, principal.getName());
        return ResponseEntity.noContent().build();
    }

}
