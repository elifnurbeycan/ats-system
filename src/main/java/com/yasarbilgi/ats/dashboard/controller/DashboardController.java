package com.yasarbilgi.ats.dashboard.controller;

import com.yasarbilgi.ats.common.response.ApiResponse;
import com.yasarbilgi.ats.dashboard.dto.response.DashboardResponseDto;
import com.yasarbilgi.ats.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/companies/{companyId}/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    // Şirketin işe alım dashboard verilerini getirir.
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponseDto>> getSummary(@PathVariable Long companyId) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSummary(companyId)));
    }
}
