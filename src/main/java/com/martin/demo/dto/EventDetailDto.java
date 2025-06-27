package com.martin.demo.dto;

import com.martin.demo.model.AttendanceStatus;
import com.martin.demo.model.EventAttendance;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class EventDetailDto {
    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String createdBy;
    private List<AttendanceDto> attendees;

    public EventDetailDto() {}

    public EventDetailDto(com.martin.demo.model.Event ev, List<EventAttendance> list) {
        this.id = ev.getId();
        this.title = ev.getTitle();
        this.description = ev.getDescription();
        this.location = ev.getLocation();
        this.startTime = ev.getStartTime();
        this.endTime = ev.getEndTime();
        this.createdBy = ev.getCreatedBy().getUsername();
        this.attendees = list.stream().map(AttendanceDto::new).collect(Collectors.toList());
    }

    // getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getCreatedBy() { return createdBy; }
    public List<AttendanceDto> getAttendees() { return attendees; }

    /** Nested DTO for attendance entries */
    public static class AttendanceDto {
        private Long userId;
        private String username;
        private AttendanceStatus status;
        private String comment;
        private LocalDateTime updatedAt;

        public AttendanceDto() {}

        public AttendanceDto(EventAttendance att) {
            this.userId = att.getUser().getId();
            this.username = att.getUser().getUsername();
            this.status = att.getStatus();
            this.comment = att.getComment();
            this.updatedAt = att.getUpdatedAt();
        }

        public Long getUserId() { return userId; }
        public String getUsername() { return username; }
        public AttendanceStatus getStatus() { return status; }
        public String getComment() { return comment; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
    }
}
