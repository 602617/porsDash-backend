package com.martin.demo.model;

import com.martin.demo.auth.AppUser;
import jakarta.persistence.*;
import org.apache.logging.log4j.util.Lazy;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
public class Event {

    @Id @GeneratedValue
    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime startTime, endTime;
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = LAZY)
    private AppUser createdBy;

    @OneToMany(mappedBy="event", cascade=CascadeType.ALL)
    private List<EventAttendance> attendees;

    public Event() {
    }

    public Event(Long id, String title, String description, String location, LocalDateTime startTime, LocalDateTime endTime, LocalDateTime createdAt, AppUser createdBy, List<EventAttendance> attendees) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.attendees = attendees;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public AppUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(AppUser createdBy) {
        this.createdBy = createdBy;
    }

    public List<EventAttendance> getAttendees() {
        return attendees;
    }

    public void setAttendees(List<EventAttendance> attendees) {
        this.attendees = attendees;
    }
}
