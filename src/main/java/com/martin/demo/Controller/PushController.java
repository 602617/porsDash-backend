package com.martin.demo.Controller;

import com.martin.demo.auth.AppUser;
import com.martin.demo.dto.PushSubscription;
import com.martin.demo.model.UserPushSubscription;
import com.martin.demo.repository.AppUserRepository;
import com.martin.demo.repository.UserPushSubscriptionRepository;
import com.martin.demo.service.PushNotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/push")
public class PushController {

    private final AppUserRepository users;
    private final UserPushSubscriptionRepository subs;
    private final PushNotificationService pushNotificationService;

    public PushController(
            AppUserRepository users,
            UserPushSubscriptionRepository subs,
            PushNotificationService pushNotificationService
    ) {
        this.users = users;
        this.subs = subs;
        this.pushNotificationService = pushNotificationService;
    }

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody PushSubscription dto, Principal p) {
        if (p == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        AppUser user = users.findByUsername(p.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (dto == null || dto.endpoint == null || dto.endpoint.isBlank() || dto.keys == null) {
            return ResponseEntity.badRequest().body("Invalid subscription");
        }

        String p256dh = dto.keys.get("p256dh");
        String auth = dto.keys.get("auth");

        if (p256dh == null || p256dh.isBlank() || auth == null || auth.isBlank()) {
            return ResponseEntity.badRequest().body("Missing keys");
        }

        var existingOpt = subs.findByEndpoint(dto.endpoint);
        if (existingOpt.isPresent()) {
            UserPushSubscription existing = existingOpt.get();
            existing.setUser(user);
            existing.setP256dh(p256dh);
            existing.setAuth(auth);
            subs.save(existing);
            return ResponseEntity.noContent().build();
        }

        UserPushSubscription s = new UserPushSubscription();
        s.setUser(user);
        s.setEndpoint(dto.endpoint);
        s.setP256dh(p256dh);
        s.setAuth(auth);
        subs.save(s);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestBody PushSubscription dto, Principal p) {
        if (p == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        if (dto == null || dto.endpoint == null || dto.endpoint.isBlank()) {
            return ResponseEntity.badRequest().body("Missing endpoint");
        }

        AppUser user = users.findByUsername(p.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        subs.findByEndpoint(dto.endpoint).ifPresent(existing -> {
            if (existing.getUser() != null && existing.getUser().getId().equals(user.getId())) {
                subs.delete(existing);
            }
        });

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/test")
    public ResponseEntity<?> sendTestPush(Principal p) {
        if (p == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        AppUser user = users.findByUsername(p.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<UserPushSubscription> userSubscriptions = subs.findAllByUserId(user.getId());

        if (userSubscriptions.isEmpty()) {
            return ResponseEntity.badRequest().body("No subscriptions found for user");
        }

        int sent = 0;
        int failed = 0;

        for (UserPushSubscription sub : userSubscriptions) {
            try {
                pushNotificationService.sendPush(
                        sub,
                        "Test notification",
                        "Push notifications are working",
                        "/"
                );
                sent++;
            } catch (Exception e) {
                failed++;

                // remove dead subscriptions if provider says endpoint is gone
                // keep this simple for now; improve later in service if you want
                String msg = e.getMessage() != null ? e.getMessage() : "";
                if (msg.contains("410") || msg.contains("404")) {
                    subs.delete(sub);
                }
            }
        }

        return ResponseEntity.ok(Map.of(
                "message", "Test push attempted",
                "sent", sent,
                "failed", failed
        ));
    }
}