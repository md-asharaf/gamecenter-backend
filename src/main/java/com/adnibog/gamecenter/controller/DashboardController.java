package com.adnibog.gamecenter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adnibog.gamecenter.dto.response.ApiResponse;
import com.adnibog.gamecenter.dto.response.DashboardStatsResponse;
import com.adnibog.gamecenter.service.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Dashboard", description = "Endpoints for dashboard statistics")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "Get Dashboard Stats", description = "Retrieves aggregated statistics for the overview dashboard.")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats(
            @RequestAttribute("adminId") String adminId) {
        DashboardStatsResponse stats = dashboardService.getDashboardStats(adminId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
