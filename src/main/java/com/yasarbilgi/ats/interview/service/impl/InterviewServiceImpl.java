package com.yasarbilgi.ats.interview.service.impl;

import com.yasarbilgi.ats.candidateprocess.entity.CandidateProcess;
import com.yasarbilgi.ats.candidateprocess.repository.CandidateProcessRepository;
import com.yasarbilgi.ats.common.exception.*;
import com.yasarbilgi.ats.interview.dto.request.*;
import com.yasarbilgi.ats.interview.dto.response.*;
import com.yasarbilgi.ats.interview.entity.*;
import com.yasarbilgi.ats.interview.mapper.InterviewMapper;
import com.yasarbilgi.ats.interview.repository.*;
import com.yasarbilgi.ats.interview.service.InterviewService;
import com.yasarbilgi.ats.user.entity.User;
import com.yasarbilgi.ats.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class InterviewServiceImpl implements InterviewService {
    private final CandidateProcessRepository processRepository;
    private final UserRepository userRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewEvaluationRepository evaluationRepository;
    private final InterviewMapper mapper;

    // Görüşmeyi süreç ve doğrulanmış şirket kullanıcılarıyla oluşturur.
    @Override @Transactional
    public InterviewResponseDto create(Long companyId, Long processId, CreateInterviewRequestDto request) {
        CandidateProcess process = getProcess(companyId, processId);
        Set<User> users = getUsers(companyId, request.interviewerIds());
        Interview interview = Interview.builder().company(process.getCompany()).candidateProcess(process)
                .type(request.type()).mode(request.mode()).status(InterviewStatus.SCHEDULED)
                .scheduledAt(request.scheduledAt()).durationMinutes(request.durationMinutes())
                .location(text(request.location())).meetingUrl(text(request.meetingUrl()))
                .interviewers(users).build();
        return mapper.toResponseDto(interviewRepository.save(interview));
    }
    // Sürecin aktif görüşmelerini planlanan zamana göre getirir.
    @Override public List<InterviewResponseDto> getAll(Long companyId, Long processId) {
        getProcess(companyId, processId);
        return interviewRepository.findAllByCompanyIdAndCandidateProcessIdAndActiveTrueOrderByScheduledAtAsc(companyId, processId)
                .stream().map(mapper::toResponseDto).toList();
    }
    // Yalnızca planlanmış görüşmenin ayrıntılarını günceller.
    @Override @Transactional
    public InterviewResponseDto update(Long companyId, Long processId, Long id, UpdateInterviewRequestDto request) {
        Interview interview = getInterview(companyId, processId, id);
        ensureScheduled(interview);
        interview.updateSchedule(request.type(), request.mode(), request.scheduledAt(), request.durationMinutes(),
                text(request.location()), text(request.meetingUrl()), getUsers(companyId, request.interviewerIds()));
        return mapper.toResponseDto(interview);
    }
    // Planlanmış görüşmeyi tamamlar veya iptal eder.
    @Override @Transactional
    public InterviewResponseDto changeStatus(Long companyId, Long processId, Long id, ChangeInterviewStatusRequestDto request) {
        Interview interview = getInterview(companyId, processId, id);
        if (interview.getStatus() == request.status()) return mapper.toResponseDto(interview);
        ensureScheduled(interview);
        if (request.status() == InterviewStatus.SCHEDULED) throw new BusinessRuleException("Görüşme yeniden planlandı durumuna alınamaz.");
        interview.changeStatus(request.status());
        return mapper.toResponseDto(interview);
    }
    // Atanmış görüşmecinin değerlendirmesini tek kayıt olacak şekilde kaydeder.
    @Override @Transactional
    public InterviewEvaluationResponseDto saveEvaluation(Long companyId, Long processId, Long id, SaveInterviewEvaluationRequestDto request) {
        Interview interview = getInterview(companyId, processId, id);
        if (interview.getStatus() == InterviewStatus.CANCELLED) throw new BusinessRuleException("İptal edilmiş görüşme değerlendirilemez.");
        User evaluator = interview.getInterviewers().stream().filter(u -> u.getId().equals(request.evaluatorUserId()))
                .findFirst().orElseThrow(() -> new BusinessRuleException("Kullanıcı bu görüşmeye görüşmeci olarak atanmamış."));
        InterviewEvaluation evaluation = evaluationRepository.findByCompanyIdAndInterviewIdAndEvaluatorId(companyId, id, evaluator.getId())
                .orElseGet(() -> InterviewEvaluation.builder().company(interview.getCompany()).interview(interview).evaluator(evaluator)
                        .score(request.score()).recommendation(request.recommendation()).feedback(request.feedback().trim()).build());
        evaluation.update(request.score(), request.recommendation(), request.feedback().trim());
        return mapper.toEvaluationResponseDto(evaluationRepository.save(evaluation));
    }
    // Görüşmenin aktif değerlendirmelerini getirir.
    @Override public List<InterviewEvaluationResponseDto> getEvaluations(Long companyId, Long processId, Long id) {
        getInterview(companyId, processId, id);
        return evaluationRepository.findAllByCompanyIdAndInterviewIdAndActiveTrueOrderByCreatedAtAsc(companyId, id)
                .stream().map(mapper::toEvaluationResponseDto).toList();
    }
    // Aday sürecini şirket sınırında aktif kayıtlardan getirir.
    private CandidateProcess getProcess(Long companyId, Long id) { return processRepository.findByCompanyIdAndId(companyId, id)
            .filter(CandidateProcess::isActive).orElseThrow(() -> new ResourceNotFoundException("Aday süreci bulunamadı.")); }
    // Görüşmeyi şirket ve süreç sınırında getirir.
    private Interview getInterview(Long companyId, Long processId, Long id) { return interviewRepository
            .findWithDetailsByCompanyIdAndCandidateProcessIdAndId(companyId, processId, id).filter(Interview::isActive)
            .orElseThrow(() -> new ResourceNotFoundException("Görüşme bulunamadı.")); }
    // Görüşmeci kimliklerinin şirkete ait aktif kullanıcılar olduğunu doğrular.
    private Set<User> getUsers(Long companyId, Set<Long> ids) { List<User> users = userRepository.findAllByCompanyIdAndIdInAndActiveTrue(companyId, ids);
        if (users.size() != ids.size()) throw new BusinessRuleException("Görüşmecilerden biri bulunamadı veya şirkete ait değil."); return new HashSet<>(users); }
    // İşlem için görüşmenin halen planlanmış olmasını doğrular.
    private void ensureScheduled(Interview interview) { if (interview.getStatus() != InterviewStatus.SCHEDULED)
        throw new BusinessRuleException("Yalnızca planlanmış görüşme değiştirilebilir."); }
    // Boş isteğe bağlı metinleri null değerine dönüştürür.
    private String text(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
