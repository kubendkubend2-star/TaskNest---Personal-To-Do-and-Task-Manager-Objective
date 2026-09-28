package com.tasknest.controller;

import com.tasknest.dto.DashboardResponseDto;
import com.tasknest.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints for fetching aggregated user dashboard statistics")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "Get user dashboard statistics")
    public ResponseEntity<DashboardResponseDto> getDashboardSummary(@PathVariable Long userId) {
        DashboardResponseDto dashboard = dashboardService.getDashboard(userId);
        return ResponseEntity.ok(dashboard);
    }
}
