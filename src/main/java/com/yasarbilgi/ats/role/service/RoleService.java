package com.yasarbilgi.ats.role.service;

import com.yasarbilgi.ats.role.dto.response.RoleResponseDto;

import java.util.List;

public interface RoleService {

    // İK tarafından kullanıcıya atanabilecek şirket rollerini listeler.
    List<RoleResponseDto> getAssignableRoles(Long companyId);
}
