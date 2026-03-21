package com.martin.demo.pushnotifications.notifications;

import com.martin.demo.pushnotifications.notifications.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<AppNotification, Long> {
    List<AppNotification> findByRecipientUsernameAndReadIsFalseOrderByCreatedAtDesc(String username);
}