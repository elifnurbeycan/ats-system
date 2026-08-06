package com.yasarbilgi.ats.interaction.repository;

import com.yasarbilgi.ats.interaction.entity.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {

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
