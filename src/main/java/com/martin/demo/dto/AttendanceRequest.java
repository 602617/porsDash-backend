package com.martin.demo.dto;

import com.martin.demo.model.AttendanceStatus;

public class AttendanceRequest {
    private AttendanceStatus status;
    private String comment;

    public AttendanceRequest() {}

    public AttendanceRequest(AttendanceStatus status, String comment) {
        this.status = status;
        this.comment = comment;
    }

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
