package com.yasarbilgi.ats.company.service;

import com.yasarbilgi.ats.company.dto.request.ChangeCompanyStatusRequestDto;
import com.yasarbilgi.ats.company.dto.request.CreateCompanyRequestDto;
import com.yasarbilgi.ats.company.dto.request.UpdateCompanyRequestDto;
import com.yasarbilgi.ats.company.dto.response.CompanyResponseDto;
import com.yasarbilgi.ats.company.dto.response.CreatedCompanyResponseDto;

import java.util.List;

public interface CompanyService {

    // Yeni şirketi, varsayılan rollerini ve ilk yönetici kullanıcılarını oluşturur.
    CreatedCompanyResponseDto create(CreateCompanyRequestDto request);

    // Sistemdeki tüm şirketleri adlarına göre sıralayarak getirir.
    List<CompanyResponseDto> getAll();

    // Belirtilen şirketin temel bilgilerini getirir.
    CompanyResponseDto getById(Long companyId);

    // Şirketin görünen adını günceller.
    CompanyResponseDto update(Long companyId, UpdateCompanyRequestDto request);

    // Şirketin kullanım durumunu değiştirir.
    CompanyResponseDto changeStatus(Long companyId, ChangeCompanyStatusRequestDto request);
}
