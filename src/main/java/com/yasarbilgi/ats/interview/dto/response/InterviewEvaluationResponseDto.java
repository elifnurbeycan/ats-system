package com.yasarbilgi.ats.interview.dto.response;
import com.yasarbilgi.ats.interview.entity.InterviewRecommendation;
import java.time.Instant;
public record InterviewEvaluationResponseDto(Long id, Long evaluatorUserId, String evaluatorFullName,
        Integer score, InterviewRecommendation recommendation, String feedback, Instant updatedAt) {}
