package com.martin.demo.pushnotifications.push;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.martijndwars.webpush.Notification;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PushService {

    private static final Logger log = LoggerFactory.getLogger(PushService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserPushSubscriptionRepository subs;
    private nl.martijndwars.webpush.PushService pushService;
    private boolean enabled = false;

    public PushService(
            UserPushSubscriptionRepository subs,
            @Value("${vapid.public.key:}") String publicKey,
            @Value("${vapid.private.key:}") String privateKey,
            @Value("${vapid.subject:}") String subject
    ) {
        this.subs = subs;

        try {
            log.info("Initializing PushNotificationService");
            log.info("public key present: {}", !publicKey.isBlank());
            log.info("private key present: {}", !privateKey.isBlank());
            log.info("subject: {}", subject);

            if (publicKey.isBlank() || privateKey.isBlank() || subject.isBlank()) {
                log.warn("Push notifications disabled: missing VAPID configuration");
                return;
            }

            Security.addProvider(new BouncyCastleProvider());

            this.pushService = new nl.martijndwars.webpush.PushService()
                    .setPublicKey(publicKey)
                    .setPrivateKey(privateKey)
                    .setSubject(subject);

            this.enabled = true;
            log.info("PushNotificationService initialized successfully");
        } catch (Exception e) {
            log.error("Push notifications disabled due to init error", e);
        }
    }

    public void sendToUser(String username, String title, String body, String url) {
        if (!enabled || pushService == null) {
            log.warn("Push skipped: service not enabled");
            return;
        }

        log.info("Sending push to username {}", username);

        List<UserPushSubscription> subscriptions = subs.findByUserUsername(username);
        log.info("Found {} subscriptions for {}", subscriptions.size(), username);

        if (subscriptions.isEmpty()) {
            log.warn("No push subscriptions found for {}", username);
            return;
        }

        for (UserPushSubscription sub : subscriptions) {
            try {
                log.info("Sending push to endpoint {}", sub.getEndpoint());
                sendPush(sub, title, body, url);
                log.info("Push sent successfully to endpoint {}", sub.getEndpoint());
            } catch (Exception e) {
                log.error("Failed to send push notification to endpoint {}", sub.getEndpoint(), e);
            }
        }
    }

    public void sendPush(UserPushSubscription sub, String title, String body, String url) throws Exception {
        if (!enabled || pushService == null) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("title", title);
        payload.put("body", body);
        payload.put("url", url);

        String jsonPayload = objectMapper.writeValueAsString(payload);

        Notification notification = new Notification(
                sub.getEndpoint(),
                sub.getP256dh(),
                sub.getAuth(),
                jsonPayload.getBytes(StandardCharsets.UTF_8)
        );

        pushService.send(notification);
    }
}