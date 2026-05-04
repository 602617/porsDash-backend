package com.martin.demo.service;

import com.martin.demo.auth.AppUser;
import com.martin.demo.dto.UserDto;
import com.martin.demo.repository.AppUserRepository;
import com.martin.demo.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class UserService {
    @Autowired private AppUserRepository userRepo;
    @Autowired private FriendshipService friendshipService;

    public UserDto findByUsername(String username) {
        AppUser user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new UserDto(user.getId(), user.getUsername());
    }

    public List<UserDto> listAll(String username) {
        Set<Long> friendIds = friendshipService.getFriendAndSelfIds(username);
        return userRepo.findAll().stream()
                .filter(u -> friendIds.contains(u.getId()))
                .map(u -> new UserDto(u.getId(), u.getUsername()))
                .toList();
    }

    public List<UserDto> search(String q) {
        return userRepo.findByUsernameContainingIgnoreCase(q).stream()
                .map(u -> new UserDto(u.getId(), u.getUsername()))
                .toList();
    }
}
