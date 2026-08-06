package com.yasarbilgi.ats.interview.repository;

import com.yasarbilgi.ats.interview.entity.Interview;
import com.yasarbilgi.ats.interview.entity.InterviewStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.time.Instant;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    // Belirli tarih aralığında planlanan aktif görüşmelerin sayısını getirir.
    long countByCompanyIdAndStatusAndScheduledAtBetweenAndActiveTrue(
            Long companyId, InterviewStatus status, Instant start, Instant end);
    // Görüşmeyi süreç ve görüşmeci ayrıntılarıyla şirket sınırında getirir.
    @EntityGraph(attributePaths = {"candidateProcess", "interviewers"})
    Optional<Interview> findWithDetailsByCompanyIdAndCandidateProcessIdAndId(
            Long companyId, Long candidateProcessId, Long interviewId);
    // Sürece ait aktif görüşmeleri tarih sırasıyla getirir.
    @EntityGraph(attributePaths = "interviewers")
    List<Interview> findAllByCompanyIdAndCandidateProcessIdAndActiveTrueOrderByScheduledAtAsc(
            Long companyId, Long candidateProcessId);
}
