package com.example.wallet.security;

import com.example.wallet.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;

/**
 * Distributed rate limiter backed by Redis.
 * Uses atomic INCR + EXPIRE to enforce a per-IP sliding window across all application instances.
 * Key format: rate:<ip>:<endpoint-slug>
 * Window: {@value WINDOW_SECONDS} seconds, max {@value MAX_ATTEMPTS} requests.
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);
    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_SECONDS = 60L;
    private static final Set<String> PROTECTED_PATHS = Set.of("/api/auth/login", "/api/auth/register");

    private final AppProperties appProperties;
    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (!PROTECTED_PATHS.contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        String endpoint = request.getRequestURI().replace("/api/auth/", "");
        String key = "rate:" + clientIp + ":" + endpoint;

        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            // First request in the window – set TTL atomically
            redisTemplate.expire(key, Duration.ofSeconds(WINDOW_SECONDS));
        }

        if (count != null && count > MAX_ATTEMPTS) {
            log.warn("Rate limit exceeded for IP: {} on /{}", clientIp, endpoint);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"status\":429,\"title\":\"TOO_MANY_REQUESTS\",\"detail\":\"Too many requests \u2013 please try again later\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Resolves the client IP address. {@code X-Forwarded-For} is only trusted when
     * {@code app.trusted-proxy=true}, i.e. the application is deployed behind a known reverse proxy.
     */
    private String resolveClientIp(HttpServletRequest request) {
        if (appProperties.trustedProxy()) {
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                return forwardedFor.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
