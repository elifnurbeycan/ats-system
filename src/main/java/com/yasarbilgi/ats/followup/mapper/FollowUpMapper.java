package com.yasarbilgi.ats.followup.mapper;

import com.yasarbilgi.ats.followup.dto.response.FollowUpResponseDto;
import com.yasarbilgi.ats.followup.entity.*;
import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
public class FollowUpMapper {
    // Takip görevini sorumlu ve gecikme bilgileriyle API yanıtına dönüştürür.
    public FollowUpResponseDto toResponseDto(FollowUp followUp) {
        boolean overdue = followUp.getStatus() == FollowUpStatus.PENDING
                && followUp.getDueAt().isBefore(Instant.now());
        return new FollowUpResponseDto(followUp.getId(), followUp.getCandidate().getId(),
                followUp.getCandidateProcess() == null ? null : followUp.getCandidateProcess().getId(),
                followUp.getAssignedTo().getId(), followUp.getAssignedTo().getFullName(),
                followUp.getTitle(), followUp.getDescription(), followUp.getDueAt(),
                followUp.getStatus(), followUp.getCompletedAt(), overdue, followUp.isActive());
    }
}
