package com.yasarbilgi.ats.contactlead.dto;
import com.yasarbilgi.ats.contactlead.entity.*;
import com.yasarbilgi.ats.interaction.entity.InteractionChannel;
import java.time.Instant;
public record ContactLeadResponse(Long id, String firstName, String lastName, String fullName, String linkedinUrl, Long positionId, String positionTitle, Long departmentId, String departmentName, Long pipelineId, String pipelineName, ContactLeadStatus status, InteractionChannel contactChannel, ContactRejectionReason rejectionReason, String note, Long candidateProcessId, Instant resolvedAt, Instant createdAt, Instant updatedAt) {}
