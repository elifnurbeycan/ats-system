package com.yasarbilgi.ats.common.handler;

import com.yasarbilgi.ats.common.exception.*;
import com.yasarbilgi.ats.common.response.ApiErrorResponse;

import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return response(ErrorCode.RESOURCE_NOT_FOUND, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessRule(BusinessRuleException exception, HttpServletRequest request) {
        return response(ErrorCode.BUSINESS_RULE_VIOLATION, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(UnauthorizedException exception, HttpServletRequest request) {
        return response(ErrorCode.UNAUTHORIZED, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(ForbiddenException exception, HttpServletRequest request) {
        return response(ErrorCode.FORBIDDEN, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return response(ErrorCode.VALIDATION_FAILED, "Gönderilen bilgiler geçersiz.", request, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedBody(HttpMessageNotReadableException exception, HttpServletRequest request) {
        log.warn("Malformed request body at {}: {}", request.getRequestURI(), exception.getMessage());
        return response(ErrorCode.MALFORMED_REQUEST_BODY, "İstek gövdesi okunamadı veya geçersiz.", request, Map.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParameter(MissingServletRequestParameterException exception, HttpServletRequest request) {
        return response(ErrorCode.MISSING_REQUEST_PARAMETER,
                "Zorunlu istek parametresi eksik: " + exception.getParameterName(), request, Map.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        return response(ErrorCode.TYPE_MISMATCH,
                "İstek parametresi geçersiz: " + exception.getName(), request, Map.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException exception, HttpServletRequest request) {
        return response(ErrorCode.METHOD_NOT_ALLOWED, "Bu endpoint belirtilen HTTP metodunu desteklemiyor.", request, Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException exception, HttpServletRequest request) {
        log.warn("Data integrity violation at {}", request.getRequestURI(), exception);
        return response(ErrorCode.DATA_INTEGRITY_VIOLATION,
                "İşlem mevcut verilerle çakıştığı için tamamlanamadı.", request, Map.of());
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockException.class})
    public ResponseEntity<ApiErrorResponse> handleOptimisticLock(Exception exception, HttpServletRequest request) {
        log.warn("Optimistic lock conflict at {}", request.getRequestURI(), exception);
        return response(ErrorCode.OPTIMISTIC_LOCK_CONFLICT,
                "Kayıt başka bir kullanıcı tarafından güncellendi. Lütfen yenileyip tekrar deneyin.", request, Map.of());
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ApiErrorResponse> handleTooManyRequests(TooManyRequestsException exception, HttpServletRequest request) {
        return ResponseEntity.status(ErrorCode.TOO_MANY_REQUESTS.getHttpStatus())
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(exception.getRetryAfterSeconds()))
                .body(body(ErrorCode.TOO_MANY_REQUESTS, exception.getMessage(), request, Map.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Unexpected error at {}", request.getRequestURI(), exception);
        return response(ErrorCode.INTERNAL_SERVER_ERROR,
                "Beklenmeyen bir hata oluştu. Lütfen daha sonra tekrar deneyin.", request, Map.of());
    }

    private ResponseEntity<ApiErrorResponse> response(ErrorCode code, String message,
                                                       HttpServletRequest request, Map<String, String> errors) {
        return ResponseEntity.status(code.getHttpStatus()).body(body(code, message, request, errors));
    }

    private ApiErrorResponse body(ErrorCode code, String message,
                                  HttpServletRequest request, Map<String, String> errors) {
        return new ApiErrorResponse(Instant.now(), code.getHttpStatus().value(), code.name(),
                message, request.getRequestURI(), errors);
    }
}
