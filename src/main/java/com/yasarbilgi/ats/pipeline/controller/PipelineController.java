package com.yasarbilgi.ats.pipeline.controller;

import com.yasarbilgi.ats.common.response.ApiResponse;
import com.yasarbilgi.ats.pipeline.dto.response.PipelineStageResponseDto;
import com.yasarbilgi.ats.pipeline.dto.response.PipelineSummaryResponseDto;
import com.yasarbilgi.ats.pipeline.service.PipelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/companies/{companyId}/pipelines")
public class PipelineController {

    private final PipelineService pipelineService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PipelineSummaryResponseDto>>> getPipelines(
            @PathVariable Long companyId
    ) {
        return ResponseEntity.ok(ApiResponse.success(pipelineService.getPipelines(companyId)));
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
}
