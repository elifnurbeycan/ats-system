package com.yasarbilgi.ats.candidateprocess.service;

import com.yasarbilgi.ats.candidateprocess.dto.request.ChangeCandidateStageRequestDto;
import com.yasarbilgi.ats.candidateprocess.dto.request.CreateCandidateProcessRequestDto;
import com.yasarbilgi.ats.candidateprocess.dto.response.CandidateProcessResponseDto;
import com.yasarbilgi.ats.candidateprocess.dto.response.PipelineBoardResponseDto;

public interface CandidateProcessService {

    CandidateProcessResponseDto create(
            Long companyId,
            CreateCandidateProcessRequestDto request
    );

    CandidateProcessResponseDto changeStage(
            Long companyId,
            Long candidateProcessId,
            ChangeCandidateStageRequestDto request
    );

    PipelineBoardResponseDto getBoard(
            Long companyId,
            Long pipelineId,
            Long positionId
    );
}
