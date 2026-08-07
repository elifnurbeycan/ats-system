package com.yasarbilgi.ats.audit.service;

import com.yasarbilgi.ats.audit.dto.AuditLogResponseDto;
import com.yasarbilgi.ats.audit.entity.AuditLog;
import com.yasarbilgi.ats.audit.repository.AuditLogRepository;
import com.yasarbilgi.ats.common.response.PageResponse;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AuditLog log) {
        repository.save(log);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponseDto> getAll(Long companyId, String action, String resourceType,
                                                    Long actorUserId, Instant from, Instant to,
                                                    int page, int size) {
        Specification<AuditLog> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("companyId"), companyId));
            if (action != null && !action.isBlank()) predicates.add(builder.equal(root.get("action"), action));
            if (resourceType != null && !resourceType.isBlank()) predicates.add(builder.equal(root.get("resourceType"), resourceType));
            if (actorUserId != null) predicates.add(builder.equal(root.get("actorUserId"), actorUserId));
            if (from != null) predicates.add(builder.greaterThanOrEqualTo(root.get("occurredAt"), from));
            if (to != null) predicates.add(builder.lessThanOrEqualTo(root.get("occurredAt"), to));
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        return PageResponse.from(repository.findAll(specification, PageRequest.of(
                Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by(Sort.Direction.DESC, "occurredAt"))), this::toDto);
    }

    private AuditLogResponseDto toDto(AuditLog log) {
        return new AuditLogResponseDto(log.getId(), log.getCompanyId(), log.getActorUserId(),
                log.getActorReference(), log.getAction(), log.getResourceType(), log.getResourceReference(),
                log.getHttpMethod(), log.getRequestPath(), log.getRequestData(), log.getResponseData(),
                log.getIpAddress(), log.getUserAgent(), log.getRequestId(), log.getHttpStatus(), log.getOccurredAt());
    }
}
