package com.martin.demo.dto;

import java.time.LocalDateTime;

public class EventListDto {
    private Long id;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String createdBy;

    public EventListDto() {}

    public EventListDto(com.martin.demo.model.Event ev) {
        this.id        = ev.getId();
        this.title     = ev.getTitle();
        this.startTime = ev.getStartTime();
        this.endTime   = ev.getEndTime();
        this.createdBy = ev.getCreatedBy().getUsername();
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
