package com.yasarbilgi.ats.candidateprocess.service;

import com.yasarbilgi.ats.candidateprocess.dto.request.ChangeCandidateStageRequestDto;
import com.yasarbilgi.ats.candidateprocess.dto.request.CreateCandidateProcessRequestDto;
import com.yasarbilgi.ats.candidateprocess.dto.request.UpdateCandidateCompensationRequestDto;
import com.yasarbilgi.ats.candidateprocess.dto.response.CandidateCompensationResponseDto;
import com.yasarbilgi.ats.candidateprocess.dto.response.CandidateProcessDetailResponseDto;
import com.yasarbilgi.ats.candidateprocess.dto.response.CandidateProcessResponseDto;
import com.yasarbilgi.ats.candidateprocess.dto.response.CandidateStageHistoryResponseDto;
import com.yasarbilgi.ats.candidateprocess.dto.response.PipelineBoardResponseDto;

import java.util.List;

public interface CandidateProcessService {

    // Yeni aday ve işe alım sürecini ilk pipeline aşamasında oluşturur.
    CandidateProcessResponseDto create(
            Long companyId,
            CreateCandidateProcessRequestDto request
    );

    // Adayı seçilen pipeline aşamasına taşır.
    CandidateProcessResponseDto changeStage(
            Long companyId,
            Long candidateProcessId,
            ChangeCandidateStageRequestDto request
    );

    // Pozisyonun pipeline board görünümünü getirir.
    PipelineBoardResponseDto getBoard(
            Long companyId,
            Long pipelineId,
            Long positionId
    );

    // Aday sürecinin maaş içermeyen detaylarını getirir.
    CandidateProcessDetailResponseDto getById(Long companyId, Long candidateProcessId);

    // Aday sürecinin ayrı tutulan hassas maaş bilgilerini getirir.
    CandidateCompensationResponseDto getCompensation(
            Long companyId,
            Long candidateProcessId
    );

    // Aday sürecinin hassas maaş bilgilerini birlikte günceller.
    CandidateCompensationResponseDto updateCompensation(
            Long companyId,
            Long candidateProcessId,
            UpdateCandidateCompensationRequestDto request
    );

    // Aday sürecinin aşama değişiklik geçmişini kronolojik olarak getirir.
    List<CandidateStageHistoryResponseDto> getStageHistory(
            Long companyId,
            Long candidateProcessId
    );
}
