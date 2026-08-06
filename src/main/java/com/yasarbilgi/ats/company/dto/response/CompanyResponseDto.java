package com.yasarbilgi.ats.company.dto.response;
import com.yasarbilgi.ats.company.entity.CompanyStatus;
public record CompanyResponseDto(Long id, String name, String code, CompanyStatus status, boolean active) {}
