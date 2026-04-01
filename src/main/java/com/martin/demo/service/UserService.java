package com.martin.demo.service;

import com.martin.demo.auth.AppUser;
import com.martin.demo.dto.UserDto;
import com.martin.demo.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired private AppUserRepository userRepo;

    public UserDto findByUsername(String username) {
        AppUser user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new UserDto(user.getId(), user.getUsername());
    }

    public List<UserDto> listAll() {
        return userRepo.findAll().stream()
                .map(u -> new UserDto(u.getId(), u.getUsername()))
                .toList();
    }
}
