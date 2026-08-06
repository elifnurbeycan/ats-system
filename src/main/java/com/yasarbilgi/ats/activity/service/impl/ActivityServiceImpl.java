package com.yasarbilgi.ats.activity.service.impl;

import com.yasarbilgi.ats.activity.dto.response.ActivityResponseDto;
import com.yasarbilgi.ats.activity.entity.ActivityType;
import com.yasarbilgi.ats.activity.service.ActivityService;
import com.yasarbilgi.ats.candidate.entity.Candidate;
import com.yasarbilgi.ats.candidate.repository.CandidateRepository;
import com.yasarbilgi.ats.candidatenote.repository.CandidateNoteRepository;
import com.yasarbilgi.ats.candidateprocess.entity.CandidateProcess;
import com.yasarbilgi.ats.candidateprocess.repository.*;
import com.yasarbilgi.ats.common.exception.*;
import com.yasarbilgi.ats.followup.repository.FollowUpRepository;
import com.yasarbilgi.ats.interaction.repository.InteractionRepository;
import com.yasarbilgi.ats.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class ActivityServiceImpl implements ActivityService {
    private final CandidateRepository candidateRepository;
    private final CandidateProcessRepository processRepository;
    private final CandidateProcessStageHistoryRepository stageHistoryRepository;
    private final CandidateNoteRepository noteRepository;
    private final InteractionRepository interactionRepository;
    private final InterviewRepository interviewRepository;
    private final FollowUpRepository followUpRepository;

    // Kaynak kayıtları ortak modele dönüştürüp en yeni hareket önce olacak şekilde birleştirir.
    @Override
    public List<ActivityResponseDto> getTimeline(Long companyId, Long candidateId,
                                                  ActivityType type, int limit) {
        validateLimit(limit);
        getCandidate(companyId, candidateId);
        List<CandidateProcess> processes = processRepository
                .findAllByCompanyIdAndCandidateIdAndActiveTrueOrderByCreatedAtDesc(companyId, candidateId);
        List<ActivityResponseDto> activities = new ArrayList<>();

        if (type == null || type == ActivityType.STAGE_CHANGE) addStageChanges(companyId, processes, activities);
        if (type == null || type == ActivityType.NOTE) addNotes(companyId, candidateId, activities);
        if (type == null || type == ActivityType.INTERACTION) addInteractions(companyId, candidateId, activities);
        if (type == null || type == ActivityType.INTERVIEW) addInterviews(companyId, processes, activities);
        if (type == null || type == ActivityType.FOLLOW_UP) addFollowUps(companyId, candidateId, activities);

        return activities.stream()
                .sorted(Comparator.comparing(ActivityResponseDto::occurredAt).reversed())
                .limit(limit)
                .toList();
    }

    // Pipeline aşama geçmişini ortak aktivite modeline ekler.
    private void addStageChanges(Long companyId, List<CandidateProcess> processes,
                                 List<ActivityResponseDto> target) {
        for (CandidateProcess process : processes) {
            stageHistoryRepository.findAllByCompanyIdAndCandidateProcessIdOrderByCreatedAtAsc(
                    companyId, process.getId()).forEach(history -> target.add(new ActivityResponseDto(
                    ActivityType.STAGE_CHANGE, history.getId(), process.getId(),
                    history.getFromStage() == null ? "Sürece eklendi" : "Aşama değiştirildi",
                    history.getFromStage() == null
                            ? history.getToStage().getName()
                            : history.getFromStage().getName() + " → " + history.getToStage().getName(),
                    history.getToStage().getStageType().name(), history.getCreatedAt(), null,
                    history.getCreatedBy())));
        }
    }

    // Aday notlarını ortak aktivite modeline ekler.
    private void addNotes(Long companyId, Long candidateId, List<ActivityResponseDto> target) {
        noteRepository.findAllByCompanyIdAndCandidateIdAndActiveTrueOrderByCreatedAtDesc(companyId, candidateId)
                .forEach(note -> target.add(new ActivityResponseDto(ActivityType.NOTE, note.getId(),
                        note.getCandidateProcess() == null ? null : note.getCandidateProcess().getId(),
                        "Aday notu eklendi", note.getContent(), null, note.getCreatedAt(), null,
                        note.getCreatedBy())));
    }

    // Aday iletişim geçmişini ortak aktivite modeline ekler.
    private void addInteractions(Long companyId, Long candidateId, List<ActivityResponseDto> target) {
        interactionRepository.findAllByCompanyIdAndCandidateIdAndActiveTrueOrderByOccurredAtDesc(companyId, candidateId)
                .forEach(item -> target.add(new ActivityResponseDto(ActivityType.INTERACTION, item.getId(),
                        item.getCandidateProcess() == null ? null : item.getCandidateProcess().getId(),
                        item.getChannel() + " iletişimi", item.getSummary(), item.getDirection().name(),
                        item.getOccurredAt(), null, item.getCreatedBy())));
    }

    // Görüşme planlarını ortak aktivite modeline ekler.
    private void addInterviews(Long companyId, List<CandidateProcess> processes,
                               List<ActivityResponseDto> target) {
        for (CandidateProcess process : processes) {
            interviewRepository.findAllByCompanyIdAndCandidateProcessIdAndActiveTrueOrderByScheduledAtAsc(
                    companyId, process.getId()).forEach(interview -> target.add(new ActivityResponseDto(
                    ActivityType.INTERVIEW, interview.getId(), process.getId(),
                    interview.getType() + " görüşmesi", interview.getMode().name(),
                    interview.getStatus().name(), interview.getCreatedAt(), interview.getScheduledAt(),
                    interview.getCreatedBy())));
        }
    }

    // Takip görevlerini ortak aktivite modeline ekler.
    private void addFollowUps(Long companyId, Long candidateId, List<ActivityResponseDto> target) {
        followUpRepository.findAllByCompanyIdAndCandidateIdAndActiveTrueOrderByDueAtAsc(companyId, candidateId)
                .forEach(item -> target.add(new ActivityResponseDto(ActivityType.FOLLOW_UP, item.getId(),
                        item.getCandidateProcess() == null ? null : item.getCandidateProcess().getId(),
                        item.getTitle(), item.getDescription(), item.getStatus().name(),
                        item.getCreatedAt(), item.getDueAt(), item.getCreatedBy())));
    }

    // Adayı şirket sınırı içerisinde aktif kayıtlardan getirir.
    private Candidate getCandidate(Long companyId, Long candidateId) {
        return candidateRepository.findByCompanyIdAndId(companyId, candidateId)
                .filter(Candidate::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Aday bulunamadı."));
    }

    // Timeline kayıt limitinin güvenli API sınırlarında olduğunu doğrular.
    private void validateLimit(int limit) {
        if (limit < 1 || limit > 200) {
            throw new BusinessRuleException("Aktivite limiti 1 ile 200 arasında olmalıdır.");
        }
    }
}
