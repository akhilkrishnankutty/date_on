package com.example.dateon.Config;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Deque<Long>> cache = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 15;
    private static final long TIME_WINDOW_MS = 60000; // 1 minute

    private boolean tryConsume(String ip) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = cache.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());
        
        synchronized (timestamps) {
            // Remove timestamps older than the time window
            while (!timestamps.isEmpty() && (now - timestamps.peekFirst() > TIME_WINDOW_MS)) {
                timestamps.pollFirst();
            }

            if (timestamps.size() < MAX_REQUESTS) {
                timestamps.addLast(now);
                return true;
            }
            return false;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/user/login") || path.startsWith("/user/create") || path.startsWith("/user/check-exists")) {
            String ip = request.getRemoteAddr();
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isEmpty()) {
                ip = forwardedFor.split(",")[0].trim();
            }

            if (tryConsume(ip)) {
                filterChain.doFilter(request, response);
            } else {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Too many requests. Please try again later.");
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
