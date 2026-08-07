package com.yasarbilgi.ats.notification.event;

public record ManagerReviewEnteredEvent(
        Long companyId,
        Long candidateProcessId,
        Long candidateId,
        String candidateName,
        String positionTitle,
        Long departmentId,
        String departmentName
) {}
