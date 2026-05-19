package com.example.wallet.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple IP-based rate limiter protecting the login endpoint against brute-force attacks.
 * Uses a sliding-window algorithm.
 */
@Component
@Order(1)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_MILLIS = 60_000L;

    private static final String LOGIN_PATH = "/api/auth/login";

    private final ConcurrentHashMap<String, Deque<Long>> requestTimestamps = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (!LOGIN_PATH.equals(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        long now = System.currentTimeMillis();

        requestTimestamps.compute(clientIp, (ipAddress, timestamps) -> {
            if (timestamps == null) {
                timestamps = new ArrayDeque<>();
            }
            // Evict timestamps outside the current window
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MILLIS) {
                timestamps.pollFirst();
            }
            timestamps.addLast(now);
            return timestamps;
        });

        Deque<Long> timestamps = requestTimestamps.get(clientIp);
        if (timestamps != null && timestamps.size() > MAX_ATTEMPTS) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"code\":\"1004\",\"message\":\"Too many requests – please try again later\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedForHeader = request.getHeader("X-Forwarded-For");
        if (forwardedForHeader != null && !forwardedForHeader.isBlank()) {
            return forwardedForHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

