package com.martin.demo.dto;

public record ApplicationPermissionDto(
        Long id,
        Long userId,
        String username,
        String role
) {}
