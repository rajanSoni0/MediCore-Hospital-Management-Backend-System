package com.hospital.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Invoked when an authenticated user tries to access a resource
 * they don't have the role for (e.g., a PATIENT hitting /api/admin/**).
 *
 * Returns a clean JSON 403 instead of the default HTML error page.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger logger = LogManager.getLogger(CustomAccessDeniedHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(
            HttpServletRequest      request,
            HttpServletResponse     response,
            AccessDeniedException   ex
    ) throws IOException {
        logger.warn("Access denied on [{}]: {}", request.getRequestURI(), ex.getMessage());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status",    403);
        body.put("error",     "Forbidden");
        body.put("message",   "You do not have permission to access this resource.");
        body.put("path",      request.getRequestURI());

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
