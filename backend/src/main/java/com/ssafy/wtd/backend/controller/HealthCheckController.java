package com.ssafy.wtd.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthCheckController {
    @GetMapping
    public String checkHealth() {
        // http://localhost:8080/api/v1/health 접속
        return "WattToDo Backend is running!";
    }
}