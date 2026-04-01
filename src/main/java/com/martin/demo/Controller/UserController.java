package com.martin.demo.Controller;

import com.martin.demo.dto.BookingDto;
import com.martin.demo.dto.UserDto;
import com.martin.demo.service.BookingService;
import com.martin.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private BookingService bookingService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        UserDto dto = userService.findByUsername(username);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public List<UserDto> listAllUsers() {
        return userService.listAll();
    }

    @GetMapping("/me/bookings")
    public List<BookingDto> getMyBookings(Authentication authentication) {
        return bookingService.findBookingsForUser(authentication.getName()).stream()
                .map(b -> new BookingDto(
                        b.getId(),
                        b.getItem().getId(),
                        b.getUser().getId(),
                        b.getUser().getUsername(),
                        b.getStartTime(),
                        b.getEndTime(),
                        b.getStatus()
                ))
                .toList();
    }
}
