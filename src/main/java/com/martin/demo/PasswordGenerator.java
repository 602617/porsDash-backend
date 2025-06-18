package com.martin.demo;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {

    public static void main(String[] args) {
        String rawPassword = "admin123";
        String encoded = new BCryptPasswordEncoder().encode(rawPassword);
        System.out.println("Encoded password:\n" + encoded);
    }
}
