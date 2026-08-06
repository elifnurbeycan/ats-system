package com.yasarbilgi.ats.interview.dto.request;

import com.yasarbilgi.ats.interview.entity.InterviewMode;
import com.yasarbilgi.ats.interview.entity.InterviewType;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.Set;

public record UpdateInterviewRequestDto(
        @NotNull InterviewType type, @NotNull InterviewMode mode,
        @NotNull Instant scheduledAt, @NotNull @Min(10) @Max(480) Integer durationMinutes,
        @Size(max = 300) String location, @Size(max = 1000) String meetingUrl,
        @NotEmpty Set<Long> interviewerIds) {}
