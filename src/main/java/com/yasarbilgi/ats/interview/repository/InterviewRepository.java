package com.yasarbilgi.ats.interview.repository;

import com.yasarbilgi.ats.interview.entity.Interview;
import com.yasarbilgi.ats.interview.entity.InterviewStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.time.Instant;
import java.util.Set;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    // Belirli tarih aralığında planlanan aktif görüşmelerin sayısını getirir.
    long countByCompanyIdAndStatusAndScheduledAtBetweenAndActiveTrue(
            Long companyId, InterviewStatus status, Instant start, Instant end);
    // İzin verilen departmanlardaki planlanmış aktif görüşmeleri tarih aralığında sayar.
    long countByCompanyIdAndCandidateProcessPositionDepartmentIdInAndStatusAndScheduledAtBetweenAndActiveTrue(
            Long companyId, Set<Long> departmentIds, InterviewStatus status, Instant start, Instant end);
    // Görüşmeyi süreç ve görüşmeci ayrıntılarıyla şirket sınırında getirir.
    @EntityGraph(attributePaths = {"candidateProcess", "interviewers"})
    Optional<Interview> findWithDetailsByCompanyIdAndCandidateProcessIdAndId(
            Long companyId, Long candidateProcessId, Long interviewId);
    // Sürece ait aktif görüşmeleri tarih sırasıyla getirir.
    @EntityGraph(attributePaths = "interviewers")
    List<Interview> findAllByCompanyIdAndCandidateProcessIdAndActiveTrueOrderByScheduledAtAsc(
            Long companyId, Long candidateProcessId);
    @EntityGraph(attributePaths = "interviewers")
    Page<Interview> findAllByCompanyIdAndCandidateProcessIdAndActiveTrue(
            Long companyId, Long candidateProcessId, Pageable pageable);

    // Görüşmecinin atandığı süreç görüşmelerini tarih sırasıyla getirir.
    @EntityGraph(attributePaths = "interviewers")
    List<Interview> findAllByCompanyIdAndCandidateProcessIdAndInterviewersIdAndActiveTrueOrderByScheduledAtAsc(
            Long companyId, Long candidateProcessId, Long interviewerId);
    @EntityGraph(attributePaths = "interviewers")
    Page<Interview> findAllByCompanyIdAndCandidateProcessIdAndInterviewersIdAndActiveTrue(
            Long companyId, Long candidateProcessId, Long interviewerId, Pageable pageable);

    // Kullanıcının görüşmeye görüşmeci olarak atanıp atanmadığını kontrol eder.
    boolean existsByCompanyIdAndCandidateProcessIdAndIdAndInterviewersIdAndActiveTrue(
            Long companyId, Long candidateProcessId, Long interviewId, Long interviewerId);

    // Kullanıcının süreçteki herhangi bir aktif görüşmeye atanıp atanmadığını kontrol eder.
    boolean existsByCompanyIdAndCandidateProcessIdAndInterviewersIdAndActiveTrue(
            Long companyId, Long candidateProcessId, Long interviewerId);
}
