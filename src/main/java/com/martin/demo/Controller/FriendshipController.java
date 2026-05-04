package com.martin.demo.Controller;

import com.martin.demo.dto.FriendshipDto;
import com.martin.demo.service.FriendshipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friendships")
public class FriendshipController {

    private final FriendshipService service;

    public FriendshipController(FriendshipService service) {
        this.service = service;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<FriendshipDto> sendRequest(@PathVariable Long userId,
                                                     Authentication auth) {
        FriendshipDto dto = service.sendRequest(auth.getName(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping
    public List<FriendshipDto> listFriends(Authentication auth) {
        return service.listFriends(auth.getName());
    }

    @GetMapping("/pending")
    public List<FriendshipDto> listPending(Authentication auth) {
        return service.listPendingRequests(auth.getName());
    }

    @PostMapping("/{id}/accept")
    public FriendshipDto accept(@PathVariable Long id, Authentication auth) {
        return service.acceptRequest(auth.getName(), id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable Long id, Authentication auth) {
        service.removeFriendship(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
