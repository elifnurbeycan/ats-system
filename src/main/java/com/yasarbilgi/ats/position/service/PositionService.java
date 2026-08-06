package com.yasarbilgi.ats.position.service;

import com.yasarbilgi.ats.position.dto.request.ChangePositionStatusRequestDto;
import com.yasarbilgi.ats.position.dto.request.CreatePositionRequestDto;
import com.yasarbilgi.ats.position.dto.request.UpdatePositionRequestDto;
import com.yasarbilgi.ats.position.dto.response.PositionResponseDto;
import com.yasarbilgi.ats.position.dto.response.PositionSummaryResponseDto;
import com.yasarbilgi.ats.position.entity.PositionStatus;

import java.util.List;

public interface PositionService {

    // Şirkete yeni bir taslak pozisyon oluşturur.
    PositionResponseDto create(Long companyId, CreatePositionRequestDto request);

    // Şirket pozisyonlarını departman ve durum filtreleriyle listeler.
    List<PositionResponseDto> getAll(
            Long companyId,
            Long departmentId,
            PositionStatus status
    );

    // Şirkete ait pozisyon detayını getirir.
    PositionResponseDto getById(Long companyId, Long positionId);

    // Pozisyonun düzenlenebilir bilgilerini günceller.
    PositionResponseDto update(
            Long companyId,
            Long positionId,
            UpdatePositionRequestDto request
    );

    // Pozisyonu izin verilen iş akışı durumuna geçirir.
    PositionResponseDto changeStatus(
            Long companyId,
            Long positionId,
            ChangePositionStatusRequestDto request
    );

    // Aday kabul eden açık pozisyonları seçim listesi için getirir.
    List<PositionSummaryResponseDto> getOpenPositions(Long companyId);
}
