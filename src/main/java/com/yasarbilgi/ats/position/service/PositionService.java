package com.yasarbilgi.ats.position.service;

import com.yasarbilgi.ats.position.dto.response.PositionSummaryResponseDto;

import java.util.List;

public interface PositionService {

    List<PositionSummaryResponseDto> getOpenPositions(Long companyId);
}
