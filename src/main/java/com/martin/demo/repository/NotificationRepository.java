package com.martin.demo.repository;

import com.martin.demo.model.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<AppNotification, Long> {
    List<AppNotification> findByRecipientUsernameAndReadIsFalseOrderByCreatedAtDesc(String username);
}