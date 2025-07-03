package com.martin.demo.dto;

import com.martin.demo.model.Notification;

import java.time.LocalDateTime;

public class NotificationDto {
    
    private Long id; 
    private String message;
    private String url;
    private LocalDateTime createdAt;

    public NotificationDto() {
    }

    public NotificationDto(Notification n) {
        this.id        = n.getId();
        this.message   = n.getMessage();
        this.url       = n.getUrl();
        this.createdAt = n.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getUrl() {
        return url;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
