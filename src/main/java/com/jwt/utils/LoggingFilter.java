package com.jwt.utils;

import com.jwt.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    private final JwtService jwtService;

    public LoggingFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String remoteAddr = request.getRemoteAddr();

        logger.info("Incoming request: URI={}, Method={}, IP={}", requestURI, method, remoteAddr);

        filterChain.doFilter(request, response);
        logJwtActions(request);

        int status = response.getStatus();
        logger.info("Outgoing response: Status={}, URI={}", status, requestURI);

        if (status == HttpServletResponse.SC_UNAUTHORIZED) {
            logger.warn("Unauthorized access attempt: URI={}, IP={}", requestURI, remoteAddr);
        }
    }

    private void logJwtActions(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            logger.info("JWT token detected: {}", token);
            try {
                String username = jwtService.extractUserName(token);
                logger.info("Token belongs to user: {}", username);
            } catch (Exception e) {
                logger.error("Failed to process JWT token: {}", e.getMessage());
            }
        }
    }
}