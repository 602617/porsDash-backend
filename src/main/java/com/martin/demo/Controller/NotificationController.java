package com.martin.demo.Controller;

import com.martin.demo.dto.NotificationDto;
import com.martin.demo.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService svc;

    public NotificationController(NotificationService svc) { this.svc = svc; }

    @GetMapping
    public List<NotificationDto> list(Principal p) {
        return svc.listUnread(p.getName())
                .stream().map(NotificationDto::new).toList();
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markRead(
            @PathVariable Long id,
            Principal p) {
        svc.markRead(id, p.getName());
        return ResponseEntity.noContent().build();
    }
}
