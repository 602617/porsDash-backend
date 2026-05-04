package com.martin.demo.dto;

import com.martin.demo.model.Friendship;
import com.martin.demo.model.FriendshipStatus;

import java.time.LocalDateTime;

public class FriendshipDto {
    private Long friendshipId;
    private Long userId;
    private String username;
    private FriendshipStatus status;
    private LocalDateTime createdAt;

    public FriendshipDto() {}

    public FriendshipDto(Friendship f, Long currentUserId) {
        this.friendshipId = f.getId();
        this.status = f.getStatus();
        this.createdAt = f.getCreatedAt();
        // Show the *other* user's info
        if (f.getRequester().getId().equals(currentUserId)) {
            this.userId = f.getAddressee().getId();
            this.username = f.getAddressee().getUsername();
        } else {
            this.userId = f.getRequester().getId();
            this.username = f.getRequester().getUsername();
        }
    }

    public Long getFriendshipId() { return friendshipId; }
    public void setFriendshipId(Long friendshipId) { this.friendshipId = friendshipId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public FriendshipStatus getStatus() { return status; }
    public void setStatus(FriendshipStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
