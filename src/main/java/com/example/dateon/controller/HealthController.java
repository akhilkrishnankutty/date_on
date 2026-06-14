package com.example.dateon.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check and keep-alive endpoint.
 * Used to keep the service alive on Render's free tier.
 */
@RestController
@RequestMapping("/keep-alive")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> keepAlive() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "alive");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "DateOn service is running");
        
        return ResponseEntity.ok(response);
    }
}
