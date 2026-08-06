package com.yasarbilgi.ats.interview.repository;

import com.yasarbilgi.ats.interview.entity.Interview;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    // Görüşmeyi süreç ve görüşmeci ayrıntılarıyla şirket sınırında getirir.
    @EntityGraph(attributePaths = {"candidateProcess", "interviewers"})
    Optional<Interview> findWithDetailsByCompanyIdAndCandidateProcessIdAndId(
            Long companyId, Long candidateProcessId, Long interviewId);
    // Sürece ait aktif görüşmeleri tarih sırasıyla getirir.
    @EntityGraph(attributePaths = "interviewers")
    List<Interview> findAllByCompanyIdAndCandidateProcessIdAndActiveTrueOrderByScheduledAtAsc(
            Long companyId, Long candidateProcessId);
}
