package com.yasarbilgi.ats.position.controller;

import com.yasarbilgi.ats.common.response.ApiResponse;
import com.yasarbilgi.ats.position.dto.response.PositionSummaryResponseDto;
import com.yasarbilgi.ats.position.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/companies/{companyId}/positions")
public class PositionController {

    private final PositionService positionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PositionSummaryResponseDto>>> getOpenPositions(
            @PathVariable Long companyId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                positionService.getOpenPositions(companyId)
        ));
    }
}
