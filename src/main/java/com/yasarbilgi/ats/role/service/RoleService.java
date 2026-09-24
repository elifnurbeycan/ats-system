package com.yasarbilgi.ats.role.service;

import com.yasarbilgi.ats.role.dto.request.CreateRoleRequestDto;
import com.yasarbilgi.ats.role.dto.request.UpdateRoleRequestDto;
import com.yasarbilgi.ats.role.dto.response.PermissionResponseDto;
import com.yasarbilgi.ats.role.dto.response.RoleResponseDto;

import java.util.List;

public interface RoleService {

    // İK tarafından kullanıcıya atanabilecek şirket rollerini listeler.
    List<RoleResponseDto> getAssignableRoles(Long companyId);

    List<PermissionResponseDto> getPermissions(Long companyId);

    RoleResponseDto create(Long companyId, CreateRoleRequestDto request);

    RoleResponseDto update(Long companyId, Long roleId, UpdateRoleRequestDto request);

    void deactivate(Long companyId, Long roleId);
}
