package com.hospital.auth;

import com.hospital.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * All DTOs for the /api/auth endpoints live here as static inner classes.
 * Keeping them co-located makes the auth contract easy to read at a glance.
 */
public class AuthDTO {

    // ---------------------------------------------------------------
    // Register
    // ---------------------------------------------------------------
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegisterRequest {

        @NotBlank(message = "Full name is required")
        private String fullName;

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        @NotNull(message = "Role is required (PATIENT / DOCTOR / ADMIN)")
        private Role role;
    }

    // ---------------------------------------------------------------
    // Login
    // ---------------------------------------------------------------
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginRequest {

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    // ---------------------------------------------------------------
    // Unified auth response (used for both register & login)
    // ---------------------------------------------------------------
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthResponse {

        private String token;          // JWT bearer token
        private String tokenType;      // Always "Bearer"
        private Long   userId;
        private String email;
        private String fullName;
        private String role;           // "PATIENT" / "DOCTOR" / "ADMIN"
        private long   expiresInMs;    // Token lifetime in milliseconds
    }
}
