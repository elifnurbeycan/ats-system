package com.yasarbilgi.ats.company.dto.response;
public record CreatedCompanyResponseDto(CompanyResponseDto company, InitialUser companyAdmin, InitialUser hrUser) {
    public record InitialUser(Long id, String fullName, String email, String roleCode) {}
}
