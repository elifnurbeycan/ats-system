package com.yasarbilgi.ats.audit.controller;

import com.yasarbilgi.ats.audit.dto.AuditLogResponseDto;
import com.yasarbilgi.ats.audit.service.AuditLogService;
import com.yasarbilgi.ats.common.response.ApiResponse;
import com.yasarbilgi.ats.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/companies/{companyId}/audit-logs")
public class AuditLogController {
    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponseDto>>> getAll(
            @PathVariable Long companyId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) Long actorUserId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                auditLogService.getAll(companyId, action, resourceType, actorUserId, from, to, page, size)));
    }
}
