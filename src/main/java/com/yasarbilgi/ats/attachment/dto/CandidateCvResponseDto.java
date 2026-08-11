package com.yasarbilgi.ats.attachment.dto;

import java.time.Instant;

public record CandidateCvResponseDto(Long id, Long candidateId, String fileName,
                                     String contentType, long fileSize, Instant uploadedAt) {
}
