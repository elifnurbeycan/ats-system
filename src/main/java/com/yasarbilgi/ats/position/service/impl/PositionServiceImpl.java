package com.yasarbilgi.ats.position.service.impl;

import com.yasarbilgi.ats.position.dto.response.PositionSummaryResponseDto;
import com.yasarbilgi.ats.position.entity.PositionStatus;
import com.yasarbilgi.ats.position.mapper.PositionMapper;
import com.yasarbilgi.ats.position.repository.PositionRepository;
import com.yasarbilgi.ats.position.service.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;
    private final PositionMapper positionMapper;

    @Override
    public List<PositionSummaryResponseDto> getOpenPositions(Long companyId) {
        log.debug("Fetching open positions for company: {}", companyId);

        return positionRepository
                .findAllByCompanyIdAndStatusAndActiveTrueOrderByTitleAsc(
                        companyId,
                        PositionStatus.OPEN
                )
                .stream()
                .map(positionMapper::toSummaryResponseDto)
                .toList();
    }
}
