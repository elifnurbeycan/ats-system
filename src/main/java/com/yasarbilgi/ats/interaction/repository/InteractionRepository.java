package com.yasarbilgi.ats.interaction.repository;

import com.yasarbilgi.ats.interaction.entity.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.yasarbilgi.ats.interaction.entity.InteractionChannel;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    @Query("""
            SELECT interaction FROM Interaction interaction
            WHERE interaction.company.id = :companyId AND interaction.candidate.id = :candidateId
              AND interaction.active = true
              AND (:candidateProcessId IS NULL OR interaction.candidateProcess.id = :candidateProcessId)
              AND (:channel IS NULL OR interaction.channel = :channel)
            """)
    Page<Interaction> searchActive(@Param("companyId") Long companyId,
                                   @Param("candidateId") Long candidateId,
                                   @Param("candidateProcessId") Long candidateProcessId,
                                   @Param("channel") InteractionChannel channel,
                                   Pageable pageable);

    // İletişim kaydını şirket ve aday sınırı içerisinde getirir.
    Optional<Interaction> findByCompanyIdAndCandidateIdAndId(
            Long companyId,
            Long candidateId,
            Long interactionId
    );

    // Adayın aktif iletişim geçmişini en yeni iletişim önce olacak şekilde getirir.
    List<Interaction> findAllByCompanyIdAndCandidateIdAndActiveTrueOrderByOccurredAtDesc(
            Long companyId,
            Long candidateId
    );

    // Belirli aday sürecinin aktif iletişim geçmişini en yeni kayıt önce getirir.
    List<Interaction>
    findAllByCompanyIdAndCandidateIdAndCandidateProcessIdAndActiveTrueOrderByOccurredAtDesc(
            Long companyId,
            Long candidateId,
            Long candidateProcessId
    );
}
