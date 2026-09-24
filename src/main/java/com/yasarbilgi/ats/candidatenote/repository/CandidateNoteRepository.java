package com.yasarbilgi.ats.candidatenote.repository;

import com.yasarbilgi.ats.candidatenote.entity.CandidateNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CandidateNoteRepository extends JpaRepository<CandidateNote, Long> {

    List<CandidateNote> findAllByCompanyIdAndCandidateIdAndActiveTrueOrderByCreatedAtDesc(
            Long companyId, Long candidateId);

    // Adaya ait notu şirket sınırı içerisinde getirir.
    Optional<CandidateNote> findByCompanyIdAndCandidateIdAndId(
            Long companyId,
            Long candidateId,
            Long noteId
    );

    // Adaya ait aktif notları en yeni not önce olacak şekilde getirir.
    Page<CandidateNote> findAllByCompanyIdAndCandidateIdAndActiveTrue(
            Long companyId,
            Long candidateId,
            Pageable pageable
    );

    Page<CandidateNote> findAllByCompanyIdAndCandidateIdAndEntryTypeAndActiveTrue(
            Long companyId, Long candidateId, String entryType, Pageable pageable);

    // Adayın belirli sürecine ait aktif notları en yeni not önce olacak şekilde getirir.
    Page<CandidateNote>
    findAllByCompanyIdAndCandidateIdAndCandidateProcessIdAndActiveTrue(
            Long companyId,
            Long candidateId,
            Long candidateProcessId,
            Pageable pageable
    );

    Page<CandidateNote> findAllByCompanyIdAndCandidateIdAndCandidateProcessIdAndEntryTypeAndActiveTrue(
            Long companyId, Long candidateId, Long candidateProcessId, String entryType, Pageable pageable);
}
