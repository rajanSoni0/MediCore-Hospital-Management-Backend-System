package com.hospital.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Invoked when an unauthenticated request hits a secured endpoint.
 *
 * Instead of the default HTML error page, we return a clean JSON 401 response —
 * which is what a REST API client expects.
 *
 * Without this, Spring Security would return an HTML redirect to /login,
 * which breaks REST clients like Postman or a React frontend.
 */
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LogManager.getLogger(JwtAuthEntryPoint.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest       request,
            HttpServletResponse      response,
            AuthenticationException  authException
    ) throws IOException {

        logger.warn("Unauthorized access attempt on [{}]: {}", request.getRequestURI(), authException.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status",    401);
        body.put("error",     "Unauthorized");
        body.put("message",   "Access denied — valid JWT token required.");
        body.put("path",      request.getRequestURI());

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
