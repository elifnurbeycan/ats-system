package com.yasarbilgi.ats.common.exception;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String message,
        Map<String, String> validationErrors
) {
}
