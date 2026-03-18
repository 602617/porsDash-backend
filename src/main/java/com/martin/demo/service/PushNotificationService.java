package com.martin.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.martin.demo.model.UserPushSubscription;
import com.martin.demo.repository.UserPushSubscriptionRepository;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PushNotificationService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PushService pushService;
    private final UserPushSubscriptionRepository subs;

    public PushNotificationService(
            UserPushSubscriptionRepository subs,
            @Value("${vapid.public.key}") String publicKey,
            @Value("${vapid.private.key}") String privateKey,
            @Value("${vapid.subject}") String subject
    ) {
        this.subs = subs;

        try {
            Security.addProvider(new BouncyCastleProvider());

            this.pushService = new PushService()
                    .setPublicKey(publicKey)
                    .setPrivateKey(privateKey)
                    .setSubject(subject);

        } catch (Exception e) {
            System.err.println("Failed to initialize PushNotificationService");
            System.err.println("vapid.public.key present: " + (publicKey != null && !publicKey.isBlank()));
            System.err.println("vapid.private.key present: " + (privateKey != null && !privateKey.isBlank()));
            System.err.println("vapid.subject: " + subject);
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize PushNotificationService", e);
        }
    }

    public void sendToUser(String username, String title, String body, String url) {
        List<UserPushSubscription> subscriptions = subs.findByUserUsername(username);

        for (UserPushSubscription sub : subscriptions) {
            try {
                sendPush(sub, title, body, url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendPush(UserPushSubscription sub, String title, String body, String url) throws Exception {
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