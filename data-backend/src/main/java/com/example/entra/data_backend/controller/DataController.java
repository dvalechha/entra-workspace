package com.example.entra.data_backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/data")
public class DataController {

    @GetMapping("/metrics")
    @PreAuthorize("hasAuthority('role.alpha')")
    public ResponseEntity<?> getMetrics() {
        Map<String, Object> data = new HashMap<>();
        data.put("source", "Data Backend");
        data.put("type", "Metrics");
        data.put("value", 1250);
        data.put("status", "Healthy");
        return ResponseEntity.ok(data);
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasAuthority('role.beta')")
    public ResponseEntity<?> getAnalytics() {
        Map<String, Object> data = new HashMap<>();
        data.put("source", "Data Backend");
        data.put("type", "Analytics");
        data.put("growth", "15%");
        data.put("users", 5400);
        return ResponseEntity.ok(data);
    }
}
