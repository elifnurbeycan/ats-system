package com.yasarbilgi.ats.dashboard.service;

import com.yasarbilgi.ats.dashboard.dto.response.DashboardResponseDto;

public interface DashboardService {

    // Şirketin işe alım operasyonlarına ait güncel dashboard özetini getirir.
    DashboardResponseDto getSummary(Long companyId);
}
