package com.yasarbilgi.ats.interview.dto.response;
import com.yasarbilgi.ats.interview.entity.*;
import java.time.Instant;
import java.util.Set;
public record InterviewResponseDto(Long id, Long candidateProcessId, InterviewType type,
        InterviewMode mode, InterviewStatus status, Instant scheduledAt, Integer durationMinutes,
        String location, String meetingUrl, Set<UserSummary> interviewers) {
    public record UserSummary(Long id, String fullName, String email) {}
}
