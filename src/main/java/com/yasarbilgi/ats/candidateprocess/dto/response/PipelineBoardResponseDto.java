package com.yasarbilgi.ats.candidateprocess.dto.response;

import java.util.List;

public record PipelineBoardResponseDto(
        Long pipelineId,
        String pipelineName,
        Long positionId,
        String positionTitle,
        List<PipelineBoardStageResponseDto> stages
) {
}
