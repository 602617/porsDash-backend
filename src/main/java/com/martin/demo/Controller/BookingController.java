package com.martin.demo.Controller;

import com.martin.demo.auth.AppUser;
import com.martin.demo.dto.BookingDto;
import com.martin.demo.dto.BookingRequest;
import com.martin.demo.model.Booking;
import com.martin.demo.repository.AppUserRepository;
import com.martin.demo.service.BookingService;
import com.martin.demo.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping
    public List<BookingDto> listBookings(@PathVariable Long itemId) {
        return bookingService.findBookingsForItem(itemId).stream()
                .map(b -> new BookingDto(
                        b.getId(),
                        b.getItem().getId(),
                        b.getUser().getId(),
                        b.getUser().getUsername(),
                        b.getStartTime(),
                        b.getEndTime()
                ))
                .toList();
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
