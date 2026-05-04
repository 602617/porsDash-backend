package com.martin.demo.model;

import com.martin.demo.auth.AppUser;
import jakarta.persistence.*;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    private AppUser requester;

    @ManyToOne(fetch = LAZY)
    private AppUser addressee;

    @Enumerated(EnumType.STRING)
    private FriendshipStatus status;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Friendship() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AppUser getRequester() { return requester; }
    public void setRequester(AppUser requester) { this.requester = requester; }

    public AppUser getAddressee() { return addressee; }
    public void setAddressee(AppUser addressee) { this.addressee = addressee; }

    public FriendshipStatus getStatus() { return status; }
    public void setStatus(FriendshipStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
