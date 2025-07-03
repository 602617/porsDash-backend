package com.martin.demo.repository;

import com.martin.demo.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
    List<Notification> findByRecipientUsernameAndReadIsFalseOrderByCreatedAtDesc(String username);
}
