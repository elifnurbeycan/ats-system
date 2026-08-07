package com.yasarbilgi.ats.audit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@Table(name = "audit_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false, updatable = false)
    private Long companyId;

    @Column(name = "actor_user_id", updatable = false)
    private Long actorUserId;

    @Column(name = "actor_reference", nullable = false, length = 200, updatable = false)
    private String actorReference;

    @Column(name = "action", nullable = false, length = 100, updatable = false)
    private String action;

    @Column(name = "resource_type", nullable = false, length = 100, updatable = false)
    private String resourceType;

    @Column(name = "resource_reference", length = 500, updatable = false)
    private String resourceReference;

    @Column(name = "http_method", nullable = false, length = 10, updatable = false)
    private String httpMethod;

    @Column(name = "request_path", nullable = false, length = 1000, updatable = false)
    private String requestPath;

    @Column(name = "request_data", columnDefinition = "TEXT", updatable = false)
    private String requestData;

    @Column(name = "response_data", columnDefinition = "TEXT", updatable = false)
    private String responseData;

    @Column(name = "ip_address", length = 64, updatable = false)
    private String ipAddress;

    @Column(name = "user_agent", length = 500, updatable = false)
    private String userAgent;

    @Column(name = "request_id", nullable = false, length = 100, updatable = false)
    private String requestId;

    @Column(name = "http_status", nullable = false, updatable = false)
    private Integer httpStatus;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Builder
    private AuditLog(Long companyId, Long actorUserId, String actorReference, String action,
                     String resourceType, String resourceReference, String httpMethod,
                     String requestPath, String requestData, String responseData,
                     String ipAddress, String userAgent, String requestId, Integer httpStatus,
                     Instant occurredAt) {
        this.companyId = companyId;
        this.actorUserId = actorUserId;
        this.actorReference = actorReference;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceReference = resourceReference;
        this.httpMethod = httpMethod;
        this.requestPath = requestPath;
        this.requestData = requestData;
        this.responseData = responseData;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.requestId = requestId;
        this.httpStatus = httpStatus;
        this.occurredAt = occurredAt;
    }
}
