package com.yasarbilgi.ats.interview.mapper;

import com.yasarbilgi.ats.interview.dto.response.*;
import com.yasarbilgi.ats.interview.entity.*;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class InterviewMapper {
    // Görüşmeyi atanmış görüşmecilerle API yanıtına dönüştürür.
    public InterviewResponseDto toResponseDto(Interview interview) {
        return new InterviewResponseDto(interview.getId(), interview.getCandidateProcess().getId(),
                interview.getType(), interview.getMode(), interview.getStatus(),
                interview.getScheduledAt(), interview.getDurationMinutes(), interview.getLocation(),
                interview.getMeetingUrl(), interview.getInterviewers().stream()
                .map(user -> new InterviewResponseDto.UserSummary(
                        user.getId(), user.getFullName(), user.getEmail()))
                .collect(Collectors.toSet()));
    }
    // Görüşmeci değerlendirmesini değerlendirici bilgisiyle API yanıtına dönüştürür.
    public InterviewEvaluationResponseDto toEvaluationResponseDto(InterviewEvaluation evaluation) {
        return new InterviewEvaluationResponseDto(evaluation.getId(), evaluation.getEvaluator().getId(),
                evaluation.getEvaluator().getFullName(), evaluation.getScore(),
                evaluation.getRecommendation(), evaluation.getFeedback(), evaluation.getUpdatedAt());
    }
}
