package com.yasarbilgi.ats.candidateprocess.repository;

import com.yasarbilgi.ats.candidateprocess.entity.CandidateProcessStageHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CandidateProcessStageHistoryRepository
        extends JpaRepository<CandidateProcessStageHistory, Long> {

    Page<CandidateProcessStageHistory>
    findAllByCompanyIdAndCandidateProcessIdOrderByCreatedAtDesc(
            Long companyId,
            Long candidateProcessId,
            Pageable pageable
    );

    Optional<CandidateProcessStageHistory>
    findFirstByCompanyIdAndCandidateProcessIdOrderByCreatedAtDesc(
            Long companyId,
            Long candidateProcessId
    );
}