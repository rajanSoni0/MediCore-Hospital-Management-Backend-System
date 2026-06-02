package com.hospital.config;

import com.hospital.security.CustomUserDetailsService;
import com.hospital.security.JwtAuthEntryPoint;
import com.hospital.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 6 / Spring Boot 3 configuration.
 *
 * Key design decisions:
 *  - NO WebSecurityConfigurerAdapter (deprecated since Spring Security 5.7)
 *  - SecurityFilterChain bean approach (component-based, testable)
 *  - Stateless session (JWT — no HttpSession needed)
 *  - CSRF disabled (REST API — clients are not browsers using cookies)
 *  - @EnableMethodSecurity for @PreAuthorize on individual controller methods
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // Enables @PreAuthorize, @PostAuthorize, @Secured
public class SecurityConfig {

    private final JwtAuthFilter              jwtAuthFilter;
    private final CustomUserDetailsService   userDetailsService;
    private final JwtAuthEntryPoint          authEntryPoint;
    private final com.hospital.security.CustomAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(
            JwtAuthFilter           jwtAuthFilter,
            CustomUserDetailsService userDetailsService,
            JwtAuthEntryPoint        authEntryPoint,
            com.hospital.security.CustomAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthFilter      = jwtAuthFilter;
        this.userDetailsService  = userDetailsService;
        this.authEntryPoint      = authEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    // ---------------------------------------------------------------
    // Core security filter chain
    // ---------------------------------------------------------------

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — REST APIs use JWT, not session cookies
            .csrf(AbstractHttpConfigurer::disable)

            // Return JSON 401/403 instead of HTML redirect on auth/authz failure
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))

            // Stateless — no HttpSession created or used
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ---------- Authorization rules ----------
            .authorizeHttpRequests(auth -> auth

                // Public — auth endpoints need no token
                .requestMatchers("/api/auth/**").permitAll()

                // ADMIN only — full management access
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Doctor management — only ADMIN can register/delete doctors
                .requestMatchers(HttpMethod.POST,   "/api/doctors").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/doctors/**").hasRole("ADMIN")

                // Patient management — ADMIN or DOCTOR can view; PATIENT can only self-access
                .requestMatchers(HttpMethod.GET,    "/api/patients/**").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers(HttpMethod.POST,   "/api/patients").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/patients/**").hasRole("ADMIN")

                // Appointments — all authenticated roles can interact
                .requestMatchers("/api/appointments/**").hasAnyRole("ADMIN", "DOCTOR", "PATIENT")

                // All other API calls require authentication
                .anyRequest().authenticated()
            )

            // Wire in our JWT filter — runs before the username/password filter
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ---------------------------------------------------------------
    // Authentication provider — wires UserDetailsService + PasswordEncoder
    // ---------------------------------------------------------------

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ---------------------------------------------------------------
    // AuthenticationManager — needed by AuthService to authenticate login
    // ---------------------------------------------------------------

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    // ---------------------------------------------------------------
    // BCrypt password encoder — cost factor 12 (production default)
    // ---------------------------------------------------------------

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
