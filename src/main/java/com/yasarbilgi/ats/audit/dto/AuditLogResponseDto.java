package com.yasarbilgi.ats.audit.dto;

import java.time.Instant;

public record AuditLogResponseDto(
        Long id,
        Long companyId,
        Long actorUserId,
        String actorReference,
        String action,
        String resourceType,
        String resourceReference,
        String httpMethod,
        String requestPath,
        String requestData,
        String responseData,
        String ipAddress,
        String userAgent,
        String requestId,
        Integer httpStatus,
        Instant occurredAt
) {
}
