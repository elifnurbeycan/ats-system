package com.yasarbilgi.ats.followup.dto.request;
import com.yasarbilgi.ats.followup.entity.FollowUpStatus;
import jakarta.validation.constraints.NotNull;
public record ChangeFollowUpStatusRequestDto(@NotNull FollowUpStatus status) {}
