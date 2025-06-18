package com.martin.demo.service;

import com.martin.demo.auth.AppUser;
import com.martin.demo.dto.UserDto;
import com.martin.demo.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired private AppUserRepository userRepo;

    public UserDto findByUsername(String username) {
        AppUser user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Map entity → DTO
        return new UserDto(
                user.getId(),
                user.getUsername()

        );
    }
}
