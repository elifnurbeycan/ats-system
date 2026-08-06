package com.yasarbilgi.ats.interview.dto.request;
import com.yasarbilgi.ats.interview.entity.InterviewStatus;
import jakarta.validation.constraints.NotNull;
public record ChangeInterviewStatusRequestDto(@NotNull InterviewStatus status) {}
