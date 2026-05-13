package com.martin.demo.dto;

import com.martin.demo.model.BookingStatus;

import java.time.LocalDateTime;

public class ActiveBookingRequestDto {
    private Long bookingId;
    private Long itemId;
    private String itemName;
    private Long requesterUserId;
    private String requesterUsername;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus status;
    private LocalDateTime updatedAt;

    public ActiveBookingRequestDto() {}

    public ActiveBookingRequestDto(Long bookingId, Long itemId, String itemName,
                                    Long requesterUserId, String requesterUsername,
                                    LocalDateTime startTime, LocalDateTime endTime,
                                    BookingStatus status, LocalDateTime updatedAt) {
        this.bookingId = bookingId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.requesterUserId = requesterUserId;
        this.requesterUsername = requesterUsername;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public Long getRequesterUserId() { return requesterUserId; }
    public void setRequesterUserId(Long requesterUserId) { this.requesterUserId = requesterUserId; }
    public String getRequesterUsername() { return requesterUsername; }
    public void setRequesterUsername(String requesterUsername) { this.requesterUsername = requesterUsername; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
