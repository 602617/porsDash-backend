package com.martin.demo.service;

import com.martin.demo.auth.AppUser;
import com.martin.demo.model.Notification;
import com.martin.demo.repository.AppUserRepository;
import com.martin.demo.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repo;
    private final AppUserRepository users;
    private final WebPushService webPush;

    public NotificationService(NotificationRepository repo,
                               AppUserRepository users,
                               WebPushService webPush) {
        this.repo = repo;
        this.users = users;
        this.webPush = webPush;
    }

    public void notifyUser(Long userId, String message, String url) {
        AppUser user = users.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Notification n = new Notification();
        n.setRecipient(user);
        n.setMessage(message);
        n.setUrl(url);
        repo.save(n);

        // 🔔 real push (if user has subscriptions)
        webPush.sendToUser(user.getUsername(), "Porsdash", message, url);
    }

    public void notifyOwner(Long ownerId, String message, String url) {
        notifyUser(ownerId, message, url);
    }

    public List<Notification> listUnread(String username) {
        return repo.findByRecipientUsernameAndReadIsFalseOrderByCreatedAtDesc(username);
    }

    public void markRead(Long notificationId, String username) {
        Notification n = repo.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Not found"));

        if (!n.getRecipient().getUsername().equals(username)) {
            throw new AccessDeniedException("Not yours");
        }

        n.setRead(true);
        repo.save(n);
    }
}
