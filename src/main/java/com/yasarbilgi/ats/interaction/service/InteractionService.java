package com.yasarbilgi.ats.interaction.service;

import com.yasarbilgi.ats.interaction.dto.request.CreateInteractionRequestDto;
import com.yasarbilgi.ats.interaction.dto.request.UpdateInteractionRequestDto;
import com.yasarbilgi.ats.interaction.dto.response.InteractionResponseDto;
import com.yasarbilgi.ats.interaction.entity.InteractionChannel;

import java.util.List;

public interface InteractionService {

    // Adaya genel veya belirli sürece bağlı iletişim kaydı ekler.
    InteractionResponseDto create(
            Long companyId,
            Long candidateId,
            CreateInteractionRequestDto request
    );

    // Aday iletişimlerini süreç ve kanal seçenekleriyle filtreleyerek listeler.
    List<InteractionResponseDto> getAll(
            Long companyId,
            Long candidateId,
            Long candidateProcessId,
            InteractionChannel channel
    );

    // İletişim kaydının ayrıntılarını günceller.
    InteractionResponseDto update(
            Long companyId,
            Long candidateId,
            Long interactionId,
            UpdateInteractionRequestDto request
    );

    // İletişim kaydını fiziksel olarak silmeden pasifleştirir.
    InteractionResponseDto deactivate(
            Long companyId,
            Long candidateId,
            Long interactionId
    );
}
