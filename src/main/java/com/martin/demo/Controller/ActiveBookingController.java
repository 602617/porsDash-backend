package com.martin.demo.Controller;

import com.martin.demo.dto.ActiveBookingRequestDto;
import com.martin.demo.service.BookingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class ActiveBookingController {

    private final BookingService bookingService;

    public ActiveBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/active-requests")
    public List<ActiveBookingRequestDto> activeRequests(Principal principal) {
        return bookingService.findActiveRequestsForOwner(principal.getName());
    }
}
