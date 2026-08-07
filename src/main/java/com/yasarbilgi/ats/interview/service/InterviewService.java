package com.yasarbilgi.ats.interview.service;
import com.yasarbilgi.ats.interview.dto.request.*;
import com.yasarbilgi.ats.interview.dto.response.*;
import java.util.List;
import com.yasarbilgi.ats.common.response.PageResponse;
public interface InterviewService {
    // Aday sürecine görüşme planlar.
    InterviewResponseDto create(Long companyId, Long processId, CreateInterviewRequestDto request);
    // Sürecin görüşmelerini listeler.
    PageResponse<InterviewResponseDto> getAll(Long companyId, Long processId, int page, int size);
    // Görüşme planını günceller.
    InterviewResponseDto update(Long companyId, Long processId, Long interviewId, UpdateInterviewRequestDto request);
    // Görüşme durumunu değiştirir.
    InterviewResponseDto changeStatus(Long companyId, Long processId, Long interviewId, ChangeInterviewStatusRequestDto request);
    // Görüşmecinin değerlendirmesini oluşturur veya günceller.
    InterviewEvaluationResponseDto saveEvaluation(Long companyId, Long processId, Long interviewId, SaveInterviewEvaluationRequestDto request);
    // Görüşmenin değerlendirmelerini listeler.
    List<InterviewEvaluationResponseDto> getEvaluations(Long companyId, Long processId, Long interviewId);
}
