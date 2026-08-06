package com.yasarbilgi.ats.candidatenote.repository;

import com.yasarbilgi.ats.candidatenote.entity.CandidateNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CandidateNoteRepository extends JpaRepository<CandidateNote, Long> {

    // Adaya ait notu şirket sınırı içerisinde getirir.
    Optional<CandidateNote> findByCompanyIdAndCandidateIdAndId(
            Long companyId,
            Long candidateId,
            Long noteId
    );

    // Adaya ait aktif notları en yeni not önce olacak şekilde getirir.
    List<CandidateNote> findAllByCompanyIdAndCandidateIdAndActiveTrueOrderByCreatedAtDesc(
            Long companyId,
            Long candidateId
    );

    // Adayın belirli sürecine ait aktif notları en yeni not önce olacak şekilde getirir.
    List<CandidateNote>
    findAllByCompanyIdAndCandidateIdAndCandidateProcessIdAndActiveTrueOrderByCreatedAtDesc(
            Long companyId,
            Long candidateId,
            Long candidateProcessId
    );
}
