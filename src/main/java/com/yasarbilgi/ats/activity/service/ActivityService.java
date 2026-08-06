package com.yasarbilgi.ats.activity.service;

import com.yasarbilgi.ats.activity.dto.response.ActivityResponseDto;
import com.yasarbilgi.ats.activity.entity.ActivityType;
import java.util.List;

public interface ActivityService {
    // Adayın farklı kaynaklardaki hareketlerini tek kronolojik akışta getirir.
    List<ActivityResponseDto> getTimeline(
            Long companyId,
            Long candidateId,
            ActivityType type,
            int limit
    );
}
