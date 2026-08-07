package com.yasarbilgi.ats.pipeline.controller;

import com.yasarbilgi.ats.common.response.ApiResponse;
import com.yasarbilgi.ats.pipeline.dto.response.PipelineStageResponseDto;
import com.yasarbilgi.ats.pipeline.dto.response.PipelineSummaryResponseDto;
import com.yasarbilgi.ats.pipeline.dto.response.PipelineDetailResponseDto;
import com.yasarbilgi.ats.pipeline.dto.request.*;
import com.yasarbilgi.ats.pipeline.service.PipelineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/companies/{companyId}/pipelines")
public class PipelineController {

    private final PipelineService pipelineService;

    // Şirkete aşamalarıyla birlikte yeni pipeline ekler.
    @PostMapping
    public ResponseEntity<ApiResponse<PipelineDetailResponseDto>> create(
            @PathVariable Long companyId, @Valid @RequestBody CreatePipelineRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pipeline oluşturuldu.", pipelineService.create(companyId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PipelineSummaryResponseDto>>> getPipelines(
            @PathVariable Long companyId
    ) {
        return ResponseEntity.ok(ApiResponse.success(pipelineService.getPipelines(companyId)));
    }

    // Pipeline ayrıntısını aşamalarıyla birlikte getirir.
    @GetMapping("/{pipelineId}")
    public ResponseEntity<ApiResponse<PipelineDetailResponseDto>> getById(
            @PathVariable Long companyId, @PathVariable Long pipelineId) {
        return ResponseEntity.ok(ApiResponse.success(pipelineService.getById(companyId, pipelineId)));
    }

    // Pipeline bilgilerini ve varsayılan durumunu günceller.
    @PutMapping("/{pipelineId}")
    public ResponseEntity<ApiResponse<PipelineDetailResponseDto>> update(
            @PathVariable Long companyId, @PathVariable Long pipelineId,
            @Valid @RequestBody UpdatePipelineRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Pipeline güncellendi.",
                pipelineService.update(companyId, pipelineId, request)));
    }

    // Kullanımda olmayan pipeline'ı pasifleştirir.
    @PatchMapping("/{pipelineId}/deactivate")
    public ResponseEntity<ApiResponse<PipelineDetailResponseDto>> deactivate(
            @PathVariable Long companyId, @PathVariable Long pipelineId) {
        return ResponseEntity.ok(ApiResponse.success("Pipeline silindi.",
                pipelineService.deactivate(companyId, pipelineId)));
    }

    @GetMapping("/{pipelineId}/stages")
    public ResponseEntity<ApiResponse<List<PipelineStageResponseDto>>> getStages(
            @PathVariable Long companyId,
            @PathVariable Long pipelineId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                pipelineService.getStages(companyId, pipelineId)
        ));
    }

    // Pipeline'a yeni aşama ekler.
    @PostMapping("/{pipelineId}/stages")
    public ResponseEntity<ApiResponse<PipelineStageResponseDto>> addStage(
            @PathVariable Long companyId, @PathVariable Long pipelineId,
            @Valid @RequestBody CreatePipelineStageRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Aşama oluşturuldu.",
                pipelineService.addStage(companyId, pipelineId, request)));
    }

    // Pipeline aşamasının bilgilerini günceller.
    @PutMapping("/{pipelineId}/stages/{stageId}")
    public ResponseEntity<ApiResponse<PipelineStageResponseDto>> updateStage(
            @PathVariable Long companyId, @PathVariable Long pipelineId, @PathVariable Long stageId,
            @Valid @RequestBody UpdatePipelineStageRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Aşama güncellendi.",
                pipelineService.updateStage(companyId, pipelineId, stageId, request)));
    }

    // Pipeline aşamalarını gönderilen sıraya göre yeniden düzenler.
    @PutMapping("/{pipelineId}/stages/order")
    public ResponseEntity<ApiResponse<List<PipelineStageResponseDto>>> reorderStages(
            @PathVariable Long companyId, @PathVariable Long pipelineId,
            @Valid @RequestBody ReorderPipelineStagesRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Aşamalar yeniden sıralandı.",
                pipelineService.reorderStages(companyId, pipelineId, request)));
    }

    // Kullanımda olmayan pipeline aşamasını pasifleştirir.
    @PatchMapping("/{pipelineId}/stages/{stageId}/deactivate")
    public ResponseEntity<ApiResponse<PipelineStageResponseDto>> deactivateStage(
            @PathVariable Long companyId, @PathVariable Long pipelineId, @PathVariable Long stageId) {
        return ResponseEntity.ok(ApiResponse.success("Aşama silindi.",
                pipelineService.deactivateStage(companyId, pipelineId, stageId)));
    }
}
