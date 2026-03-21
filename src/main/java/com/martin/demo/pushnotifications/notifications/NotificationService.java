package com.martin.demo.pushnotifications.notifications;

import com.martin.demo.auth.AppUser;
import com.martin.demo.pushnotifications.push.PushService;
import com.martin.demo.repository.AppUserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repo;
    private final AppUserRepository users;
    private final PushService webPush;

    public NotificationService(NotificationRepository repo,
                               AppUserRepository users,
                               PushService webPush) {
        this.repo = repo;
        this.users = users;
        this.webPush = webPush;
    }

    public void notifyUser(Long userId, String message, String url) {
        AppUser user = users.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        AppNotification n = new AppNotification(user, message, url);
        repo.save(n);

        webPush.sendToUser(user.getUsername(), "Porsdash", message, url);
    }

    public void notifyOwner(Long ownerId, String message, String url) {
        notifyUser(ownerId, message, url);
    }

    public List<AppNotification> listUnread(String username) {
        return repo.findByRecipientUsernameAndReadIsFalseOrderByCreatedAtDesc(username);
    }

    public void markRead(Long notificationId, String username) {
        AppNotification n = repo.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Not found"));

        if (!n.getRecipient().getUsername().equals(username)) {
            throw new AccessDeniedException("Not yours");
        }

        n.setRead(true);
        repo.save(n);
    }
}