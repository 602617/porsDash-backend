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

    public NotificationService(NotificationRepository repo, AppUserRepository users) {
        this.repo  = repo;
        this.users = users;
    }

    public void notifyOwner(Long ownerId, String message, String url) {
        AppUser owner = users.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Notification n = new Notification();
        n.setRecipient(owner);
        n.setMessage(message);
        n.setUrl(url);
        repo.save(n);
    }

    public void notifyUser(Long userId, String message, String url) {
        AppUser user = users.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Notification n = new Notification();
        n.setRecipient(user);
        n.setMessage(message);
        n.setUrl(url);
        repo.save(n);
    }

    public List<Notification> listUnread(String username) {
        return repo.findByRecipientUsernameAndReadIsFalseOrderByCreatedAtDesc(username);
    }

    public void markRead(Long notificationId, String username) {
        Notification n = repo.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Not found"));
        if (!n.getRecipient().getUsername().equals(username))
            throw new AccessDeniedException("Not yours");
        n.setRead(true);
        repo.save(n);
    }
}
