package com.hospital.security;

import com.hospital.model.User;
import com.hospital.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter — runs once per request before Spring Security's
 * UsernamePasswordAuthenticationFilter.
 *
 * Flow:
 *  1. Extract "Authorization: Bearer <token>" header
 *  2. Parse and validate the JWT
 *  3. Load the user from DB and set them into the SecurityContext
 *  4. Continue the filter chain
 *
 * If the token is missing or invalid, the filter simply passes through
 * and Spring Security will reject the request at the authorization step.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LogManager.getLogger(JwtAuthFilter.class);

    private static final String AUTH_HEADER  = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil        jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil        = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest  request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain         filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTH_HEADER);

        // No token — skip this filter, let Spring Security handle it
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt   = authHeader.substring(BEARER_PREFIX.length());
        final String email;
        try {
            email = jwtUtil.extractEmail(jwt);
        } catch (Exception ex) {
            logger.warn("Could not extract email from JWT: {}", ex.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // Only authenticate if not already authenticated in this request
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null && jwtUtil.isTokenValid(jwt, user)) {

                // Build an authenticated token with the user's granted authorities
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,                   // credentials — not needed post-authentication
                                user.getAuthorities()   // ["ROLE_ADMIN"] / ["ROLE_DOCTOR"] / ["ROLE_PATIENT"]
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Store in the SecurityContext — the request is now authenticated
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.debug("Authenticated user '{}' with role '{}'", email, user.getRole());
            }
        }

        filterChain.doFilter(request, response);
    }
}
