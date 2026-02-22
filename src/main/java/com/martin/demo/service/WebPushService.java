package com.martin.demo.service;

import com.martin.demo.model.UserPushSubscription;
import com.martin.demo.repository.UserPushSubscriptionRepository;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class WebPushService {

    @Value("${vapid.public}")
    private String vapidPublic;

    @Value("${vapid.private}")
    private String vapidPrivate;

    @Value("${vapid.subject}")
    private String vapidSubject;

    private final UserPushSubscriptionRepository subsRepo;

    public WebPushService(UserPushSubscriptionRepository subsRepo) {
        this.subsRepo = subsRepo;
    }

    public void sendToUser(String username, String title, String body, String url) {
        List<UserPushSubscription> subs = subsRepo.findByUserUsername(username);
        if (subs.isEmpty()) return;

        try {
            PushService pushService = new PushService(vapidPublic, vapidPrivate, vapidSubject); // ctor exists :contentReference[oaicite:3]{index=3}

            String payload = """
                {"title":%s,"body":%s,"url":%s}
                """.formatted(json(title), json(body), json(url));

            for (UserPushSubscription s : subs) {
                try {
                    Notification notification = new Notification(
                            s.getEndpoint(),
                            s.getP256dh(),
                            s.getAuth(),
                            payload.getBytes(StandardCharsets.UTF_8)
                    );

                    pushService.send(notification);
                } catch (Exception perSubEx) {
                    // subscription is often expired/invalid -> delete it
                    subsRepo.deleteByEndpoint(s.getEndpoint());
                }
            }
        } catch (Exception e) {
            // If this fails, check VAPID keys + subject config
            throw new RuntimeException("Failed to send push", e);
        }
    }

    // Tiny JSON escaper to avoid broken payloads if you have quotes/newlines
    private static String json(String s) {
        if (s == null) return "null";
        return "\"" + s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r") + "\"";
    }
}
