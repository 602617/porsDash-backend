package com.martin.demo.Controller;

import com.martin.demo.auth.AppUser;
import com.martin.demo.dto.PushSubscription;
import com.martin.demo.model.UserPushSubscription;
import com.martin.demo.repository.AppUserRepository;
import com.martin.demo.repository.UserPushSubscriptionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

        if (dto == null || dto.endpoint == null || dto.keys == null) {
            return ResponseEntity.badRequest().body("Invalid subscription");
        }

        String p256dh = dto.keys.get("p256dh");
        String auth = dto.keys.get("auth");

        if (p256dh == null || auth == null) {
            return ResponseEntity.badRequest().body("Missing keys");
        }

        // de-dupe by endpoint
        if (!subs.existsByEndpoint(dto.endpoint)) {
            UserPushSubscription s = new UserPushSubscription();
            s.setUser(user);
            s.setEndpoint(dto.endpoint);
            s.setP256dh(p256dh);
            s.setAuth(auth);
            subs.save(s);
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestBody PushSubscription dto) {
        if (dto != null && dto.endpoint != null) {
            subs.deleteByEndpoint(dto.endpoint);
        }
        return ResponseEntity.noContent().build();
    }
}

