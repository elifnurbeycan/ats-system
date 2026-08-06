package com.yasarbilgi.ats.candidateprocess.repository;

import com.yasarbilgi.ats.candidateprocess.entity.CandidateProcessStageHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
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

    // Sürecin aşama geçmişini eski kayıttan yeni kayda doğru aşama detaylarıyla getirir.
    @EntityGraph(attributePaths = {"fromStage", "toStage"})
    List<CandidateProcessStageHistory>
    findAllByCompanyIdAndCandidateProcessIdOrderByCreatedAtAsc(
            Long companyId,
            Long candidateProcessId
    );
}
