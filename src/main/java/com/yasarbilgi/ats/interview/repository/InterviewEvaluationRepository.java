package com.yasarbilgi.ats.interview.repository;

import com.yasarbilgi.ats.interview.entity.InterviewEvaluation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InterviewEvaluationRepository extends JpaRepository<InterviewEvaluation, Long> {
    // Görüşmecinin görüşmeye ait mevcut değerlendirmesini getirir.
    Optional<InterviewEvaluation> findByCompanyIdAndInterviewIdAndEvaluatorId(
            Long companyId, Long interviewId, Long evaluatorId);
    // Görüşmenin değerlendirmelerini değerlendirici bilgileriyle getirir.
    @EntityGraph(attributePaths = "evaluator")
    List<InterviewEvaluation> findAllByCompanyIdAndInterviewIdAndActiveTrueOrderByCreatedAtAsc(
            Long companyId, Long interviewId);
    // Görüşmecinin yalnızca kendi aktif değerlendirmesini getirir.
    List<InterviewEvaluation> findAllByCompanyIdAndInterviewIdAndEvaluatorIdAndActiveTrueOrderByCreatedAtAsc(
            Long companyId, Long interviewId, Long evaluatorId);
}
