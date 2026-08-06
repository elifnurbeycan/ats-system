package com.yasarbilgi.ats.company.dto.request;
import com.yasarbilgi.ats.company.entity.CompanyStatus;
import jakarta.validation.constraints.NotNull;
public record ChangeCompanyStatusRequestDto(@NotNull CompanyStatus status) {}
