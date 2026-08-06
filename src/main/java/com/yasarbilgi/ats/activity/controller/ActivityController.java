package com.yasarbilgi.ats.activity.controller;

import com.yasarbilgi.ats.activity.dto.response.ActivityResponseDto;
import com.yasarbilgi.ats.activity.entity.ActivityType;
import com.yasarbilgi.ats.activity.service.ActivityService;
import com.yasarbilgi.ats.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/v1/companies/{companyId}/candidates/{candidateId}/activities")
public class ActivityController {
    private final ActivityService service;
    // Adayın birleşik aktivite akışını tür ve kayıt limitiyle getirir.
    @GetMapping
    public ResponseEntity<ApiResponse<List<ActivityResponseDto>>> getTimeline(
            @PathVariable Long companyId, @PathVariable Long candidateId,
            @RequestParam(required = false) ActivityType type,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(ApiResponse.success(
                service.getTimeline(companyId, candidateId, type, limit)));
    }
}
