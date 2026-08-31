package com.martin.demo.service;

import com.martin.demo.auth.AppUser;
import com.martin.demo.dto.FriendshipDto;
import com.martin.demo.model.Friendship;
import com.martin.demo.model.FriendshipStatus;
import com.martin.demo.model.ScoringAction;
import com.martin.demo.pushnotifications.notifications.NotificationService;
import com.martin.demo.repository.AppUserRepository;
import com.martin.demo.repository.FriendshipRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class FriendshipService {

    private final FriendshipRepository friendships;
    private final AppUserRepository users;
    private final NotificationService notificationService;
    private final ScoreService scoreService;

    public FriendshipService(FriendshipRepository friendships,
                             AppUserRepository users,
                             NotificationService notificationService,
                             ScoreService scoreService) {
        this.friendships = friendships;
        this.users = users;
        this.notificationService = notificationService;
        this.scoreService = scoreService;
    }

    private AppUser me(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public Set<Long> getFriendAndSelfIds(String username) {
        AppUser me = me(username);
        Set<Long> ids = new HashSet<>();
        ids.add(me.getId());
        for (Friendship f : friendships.findAllByUserAndStatus(me.getId(), FriendshipStatus.ACCEPTED)) {
            ids.add(f.getRequester().getId());
            ids.add(f.getAddressee().getId());
        }
        return ids;
    }

    public boolean areFriends(String username, Long otherUserId) {
        return getFriendAndSelfIds(username).contains(otherUserId);
    }

    public FriendshipDto sendRequest(String username, Long friendId) {
        AppUser me = me(username);

        if (me.getId().equals(friendId)) {
            throw new IllegalArgumentException("Du kan ikke legge til deg selv som venn");
        }

        AppUser friend = users.findById(friendId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Check if a friendship already exists in either direction
        Optional<Friendship> existing = friendships.findByRequesterAndAddressee(me, friend);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Venneforespørsel finnes allerede");
        }
        Optional<Friendship> reverse = friendships.findByRequesterAndAddressee(friend, me);
        if (reverse.isPresent()) {
            throw new IllegalArgumentException("Denne brukeren har allerede sendt deg en forespørsel");
        }

        Friendship f = new Friendship();
        f.setRequester(me);
        f.setAddressee(friend);
        f.setStatus(FriendshipStatus.PENDING);
        Friendship saved = friendships.save(f);

        scoreService.award(username, ScoringAction.SEND_FRIEND_REQUEST);

        notificationService.notifyUser(friend.getId(),
                me.getUsername() + " sendte deg en venneforespørsel",
                "/friends");

        return new FriendshipDto(saved, me.getId());
    }

    public FriendshipDto acceptRequest(String username, Long friendshipId) {
        AppUser me = me(username);
        Friendship f = friendships.findById(friendshipId)
                .orElseThrow(() -> new EntityNotFoundException("Friendship not found"));

        if (!f.getAddressee().getId().equals(me.getId())) {
            throw new AccessDeniedException("Bare mottakeren kan godta forespørselen");
        }
        if (f.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalArgumentException("Forespørselen er allerede behandlet");
        }

        f.setStatus(FriendshipStatus.ACCEPTED);
        Friendship saved = friendships.save(f);

        scoreService.award(username, ScoringAction.ACCEPT_FRIEND_REQUEST);

        notificationService.notifyUser(f.getRequester().getId(),
                me.getUsername() + " godtok venneforespørselen din",
                "/friends");

        return new FriendshipDto(saved, me.getId());
    }

    public void removeFriendship(String username, Long friendshipId) {
        AppUser me = me(username);
        Friendship f = friendships.findById(friendshipId)
                .orElseThrow(() -> new EntityNotFoundException("Friendship not found"));

        boolean isRequester = f.getRequester().getId().equals(me.getId());
        boolean isAddressee = f.getAddressee().getId().equals(me.getId());
        if (!isRequester && !isAddressee) {
            throw new AccessDeniedException("Not allowed");
        }

        friendships.delete(f);
    }

    public List<FriendshipDto> listFriends(String username) {
        AppUser me = me(username);
        return friendships.findAllByUserAndStatus(me.getId(), FriendshipStatus.ACCEPTED)
                .stream()
                .map(f -> new FriendshipDto(f, me.getId()))
                .toList();
    }

    public List<FriendshipDto> listPendingRequests(String username) {
        AppUser me = me(username);
        return friendships.findByAddresseeAndStatus(me, FriendshipStatus.PENDING)
                .stream()
                .map(f -> new FriendshipDto(f, me.getId()))
                .toList();
    }
}
