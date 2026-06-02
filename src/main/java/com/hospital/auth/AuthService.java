package com.hospital.auth;

import com.hospital.exception.DuplicateResourceException;
import com.hospital.model.User;
import com.hospital.repository.UserRepository;
import com.hospital.security.JwtUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService — handles user registration and login.
 *
 * Registration flow:
 *   1. Check email uniqueness
 *   2. Hash the password with BCrypt
 *   3. Save User entity
 *   4. Generate + return JWT
 *
 * Login flow:
 *   1. Delegate to Spring Security's AuthenticationManager
 *      (it calls CustomUserDetailsService + BCrypt comparison internally)
 *   2. On success, generate + return JWT
 *   3. On failure, AuthenticationManager throws BadCredentialsException
 */
@Service
@Transactional
public class AuthService {

    private static final Logger logger = LogManager.getLogger(AuthService.class);

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtUtil               jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository        userRepository,
            PasswordEncoder       passwordEncoder,
            JwtUtil               jwtUtil,
            AuthenticationManager authenticationManager) {
        this.userRepository        = userRepository;
        this.passwordEncoder       = passwordEncoder;
        this.jwtUtil               = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    // ---------------------------------------------------------------
    // Register
    // ---------------------------------------------------------------

    public AuthDTO.AuthResponse register(AuthDTO.RegisterRequest request) {
        logger.info("Registering new user: email={}, role={}", request.getEmail(), request.getRole());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "An account with email '" + request.getEmail() + "' already exists.");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))  // BCrypt hash
                .role(request.getRole())
                .build();

        User saved = userRepository.save(user);
        logger.info("User registered successfully: id={}, role={}", saved.getId(), saved.getRole());

        String token = jwtUtil.generateToken(saved);
        return buildAuthResponse(saved, token);
    }

    // ---------------------------------------------------------------
    // Login
    // ---------------------------------------------------------------

    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest request) {
        logger.info("Login attempt: email={}", request.getEmail());

        try {
            // AuthenticationManager validates credentials using CustomUserDetailsService + BCrypt
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            User user = (User) auth.getPrincipal();
            String token = jwtUtil.generateToken(user);

            logger.info("Login successful: email={}, role={}", user.getEmail(), user.getRole());
            return buildAuthResponse(user, token);

        } catch (BadCredentialsException ex) {
            logger.warn("Login failed for email={}: invalid credentials", request.getEmail());
            // Re-throw as BadCredentialsException — GlobalExceptionHandler maps this to 401
            throw ex;
        }
    }

    // ---------------------------------------------------------------
    // Helper
    // ---------------------------------------------------------------

    private AuthDTO.AuthResponse buildAuthResponse(User user, String token) {
        return AuthDTO.AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .expiresInMs(jwtUtil.getExpirationMs())
                .build();
    }
}
