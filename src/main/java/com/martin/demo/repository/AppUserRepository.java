package com.martin.demo.repository;

import com.martin.demo.auth.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long>  {
    Optional<AppUser> findByUsername(String username);
    List<AppUser> findByUsernameContainingIgnoreCase(String query);
}
