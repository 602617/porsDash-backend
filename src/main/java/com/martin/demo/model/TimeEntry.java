package com.martin.demo.model;

import com.martin.demo.auth.AppUser;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class TimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private AppUser user;

    @Column(nullable = false)
    private Instant startTime;

    private Instant endTime;

    private Long totalMinutes;

    private Long overtimeMinutes = 0L;

    private String note;

    private Instant createdAt = Instant.now();

    public Long getId() { return id; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }

    public Long getTotalMinutes() { return totalMinutes; }
    public void setTotalMinutes(Long totalMinutes) { this.totalMinutes = totalMinutes; }

    public Long getOvertimeMinutes() { return overtimeMinutes; }
    public void setOvertimeMinutes(Long overtimeMinutes) { this.overtimeMinutes = overtimeMinutes; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Instant getCreatedAt() { return createdAt; }
}
