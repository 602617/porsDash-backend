package com.martin.demo.service;

import com.martin.demo.auth.AppUser;
import com.martin.demo.dto.UserDto;
import com.martin.demo.repository.AppUserRepository;
import com.martin.demo.repository.ApplicationPermissionRepository;
import com.martin.demo.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class UserService {
    @Autowired private AppUserRepository userRepo;
    @Autowired private FriendshipService friendshipService;
    @Autowired private ApplicationPermissionRepository permissionRepo;

    public UserDto findByUsername(String username) {
        AppUser user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<String> appRoles = permissionRepo.findByUserId(user.getId()).stream()
                .map(p -> p.getRole().name())
                .toList();
        return new UserDto(user.getId(), user.getUsername(), user.getRole(), appRoles);
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
