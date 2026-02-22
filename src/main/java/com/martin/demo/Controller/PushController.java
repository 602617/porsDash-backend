package com.martin.demo.Controller;

import com.martin.demo.auth.AppUser;
import com.martin.demo.dto.PushSubscription;
import com.martin.demo.model.UserPushSubscription;
import com.martin.demo.repository.AppUserRepository;
import com.martin.demo.repository.UserPushSubscriptionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/push")
public class PushController {

    private final AppUserRepository users;
    private final UserPushSubscriptionRepository subs;

    public PushController(AppUserRepository users, UserPushSubscriptionRepository subs) {
        this.users = users;
        this.subs = subs;
    }

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody PushSubscription dto, Principal p) {
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

        // De-dupe by endpoint
        var existingOpt = subs.findByEndpoint(dto.endpoint);
        if (existingOpt.isPresent()) {
            UserPushSubscription existing = existingOpt.get();
            // If endpoint already exists, just ensure it belongs to this user + keys are updated
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
        // optional: require logged-in, but no need to check ownership strictly
        if (dto != null && dto.endpoint != null && !dto.endpoint.isBlank()) {
            subs.deleteByEndpoint(dto.endpoint);
        }
        return ResponseEntity.noContent().build();
    }
}
