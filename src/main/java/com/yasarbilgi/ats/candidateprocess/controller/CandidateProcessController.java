package com.yasarbilgi.ats.candidateprocess.controller;

import com.yasarbilgi.ats.candidateprocess.dto.request.ChangeCandidateStageRequestDto;
import com.yasarbilgi.ats.candidateprocess.dto.request.CreateCandidateProcessRequestDto;
import com.yasarbilgi.ats.candidateprocess.dto.response.CandidateProcessResponseDto;
import com.yasarbilgi.ats.candidateprocess.dto.response.PipelineBoardResponseDto;
import com.yasarbilgi.ats.candidateprocess.service.CandidateProcessService;
import com.yasarbilgi.ats.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/companies/{companyId}")
public class CandidateProcessController {

    private final CandidateProcessService candidateProcessService;

    @PostMapping("/candidate-processes")
    public ResponseEntity<ApiResponse<CandidateProcessResponseDto>> create(
            @PathVariable Long companyId,
            @Valid @RequestBody CreateCandidateProcessRequestDto request
    ) {
        CandidateProcessResponseDto created = candidateProcessService.create(companyId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Aday süreci oluşturuldu.", created));
    }

    @PatchMapping("/candidate-processes/{candidateProcessId}/stage")
    public ResponseEntity<ApiResponse<CandidateProcessResponseDto>> changeStage(
            @PathVariable Long companyId,
            @PathVariable Long candidateProcessId,
            @Valid @RequestBody ChangeCandidateStageRequestDto request
    ) {
        CandidateProcessResponseDto updated = candidateProcessService.changeStage(
                companyId,
                candidateProcessId,
                request
        );
        return ResponseEntity.ok(ApiResponse.success("Aday aşaması güncellendi.", updated));
    }

    @GetMapping("/pipelines/{pipelineId}/positions/{positionId}/board")
    public ResponseEntity<ApiResponse<PipelineBoardResponseDto>> getBoard(
            @PathVariable Long companyId,
            @PathVariable Long pipelineId,
            @PathVariable Long positionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                candidateProcessService.getBoard(companyId, pipelineId, positionId)
        ));
    }
}
