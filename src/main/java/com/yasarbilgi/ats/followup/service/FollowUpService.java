package com.yasarbilgi.ats.followup.service;
import com.yasarbilgi.ats.followup.dto.request.*;
import com.yasarbilgi.ats.followup.dto.response.FollowUpResponseDto;
import com.yasarbilgi.ats.followup.entity.FollowUpStatus;
import java.util.List;
import com.yasarbilgi.ats.common.response.PageResponse;
public interface FollowUpService {
    // Aday için sorumlu ve son tarih içeren takip görevi oluşturur.
    FollowUpResponseDto create(Long companyId, Long candidateId, CreateFollowUpRequestDto request);
    // Adayın takip görevlerini durum ve sorumluya göre listeler.
    PageResponse<FollowUpResponseDto> getAll(Long companyId, Long candidateId, FollowUpStatus status,
                                             Long assignedToUserId, int page, int size);
    // Bekleyen takip görevinin ayrıntılarını günceller.
    FollowUpResponseDto update(Long companyId, Long candidateId, Long followUpId, UpdateFollowUpRequestDto request);
    // Takip görevini tamamlar veya iptal eder.
    FollowUpResponseDto changeStatus(Long companyId, Long candidateId, Long followUpId, ChangeFollowUpStatusRequestDto request);
}
