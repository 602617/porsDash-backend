package com.martin.demo.dto;

import com.martin.demo.model.AttendanceStatus;

import java.time.LocalDateTime;

public class AttendanceDto {
    private Long userId;
    private String username;
    private AttendanceStatus status;
    private String comment;
    private LocalDateTime updatedAt;

    public AttendanceDto(com.martin.demo.model.EventAttendance att) {
        this.userId    = att.getUser().getId();
        this.username  = att.getUser().getUsername();
        this.status    = att.getStatus();
        this.comment   = att.getComment();
        this.updatedAt = att.getUpdatedAt();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
