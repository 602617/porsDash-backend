package com.martin.demo.dto;

import java.util.List;

public record UserProfileDto(
        Long id,
        String username,
        String role,
        List<String> applicationRoles
) {}
