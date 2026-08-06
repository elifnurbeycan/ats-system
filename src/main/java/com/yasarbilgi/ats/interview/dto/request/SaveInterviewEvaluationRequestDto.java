package com.yasarbilgi.ats.interview.dto.request;
import com.yasarbilgi.ats.interview.entity.InterviewRecommendation;
import jakarta.validation.constraints.*;
public record SaveInterviewEvaluationRequestDto(
        @NotNull Long evaluatorUserId, @NotNull @Min(1) @Max(5) Integer score,
        @NotNull InterviewRecommendation recommendation,
        @NotBlank @Size(max = 5000) String feedback) {}
