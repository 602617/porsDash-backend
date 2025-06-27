package com.martin.demo.model;

import com.martin.demo.auth.AppUser;
import jakarta.persistence.*;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
public class EventAttendance {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = LAZY)
    private Event event;

    @ManyToOne(fetch = LAZY)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status; // CAN, CANNOT

    private String comment;
    private LocalDateTime updatedAt = LocalDateTime.now();

    public EventAttendance() {
    }

    public EventAttendance(Long id, Event event, AppUser user, AttendanceStatus status, String comment, LocalDateTime updatedAt) {
        this.id = id;
        this.event = event;
        this.user = user;
        this.status = status;
        this.comment = comment;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
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
