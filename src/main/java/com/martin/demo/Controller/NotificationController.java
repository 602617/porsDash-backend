package com.martin.demo.Controller;

import com.martin.demo.auth.AppUser;
import com.martin.demo.dto.NotificationDto;
import com.martin.demo.repository.AppUserRepository;
import com.martin.demo.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService svc;
    private final AppUserRepository users;

    public NotificationController(NotificationService svc, AppUserRepository users) {
        this.svc = svc;
        this.users = users;
    }

    // Existing: list unread (in-app inbox)
    @GetMapping
    public List<NotificationDto> list(Principal p) {
        return svc.listUnread(p.getName())
                .stream()
                .map(NotificationDto::new)
                .toList();
    }

    // Existing: mark as read
    @PostMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Long id, Principal p) {
        svc.markRead(id, p.getName());
        return ResponseEntity.noContent().build();
    }

    // NEW: Test push to yourself (great for Postman)
    @PostMapping("/test")
    public ResponseEntity<?> testPush(@RequestBody TestNotificationRequest req, Principal p) {

        AppUser me = users.findByUsername(p.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String message = (req.getMessage() == null || req.getMessage().isBlank())
                ? "Test notification from backend"
                : req.getMessage();

        String url = (req.getUrl() == null || req.getUrl().isBlank())
                ? "/"
                : req.getUrl();

        svc.notifyUser(me.getId(), message, url);
        return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
    }

    // OPTIONAL: Send to specific userId (lock down or remove later)
    @PostMapping("/user/{userId}")
    public ResponseEntity<?> sendToUser(@PathVariable Long userId,
                                        @RequestBody TestNotificationRequest req) {

        String message = (req.getMessage() == null || req.getMessage().isBlank())
                ? "Notification"
                : req.getMessage();

        String url = (req.getUrl() == null || req.getUrl().isBlank())
                ? "/"
                : req.getUrl();

        svc.notifyUser(userId, message, url);
        return ResponseEntity.status(HttpStatus.CREATED).body("Sent");
    }

    // Simple request body DTO
    public static class TestNotificationRequest {
        private String message;
        private String url;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
}
