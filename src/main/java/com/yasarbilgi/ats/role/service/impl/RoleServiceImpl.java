package com.yasarbilgi.ats.role.service.impl;

import com.yasarbilgi.ats.common.exception.ResourceNotFoundException;
import com.yasarbilgi.ats.company.repository.CompanyRepository;
import com.yasarbilgi.ats.role.dto.response.RoleResponseDto;
import com.yasarbilgi.ats.role.mapper.RoleMapper;
import com.yasarbilgi.ats.role.repository.RoleRepository;
import com.yasarbilgi.ats.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private static final String COMPANY_ADMIN_ROLE_CODE = "COMPANY_ADMIN";

    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    // İK kullanıcı yönetiminde seçilebilen rolleri getirir; şirket yöneticisi rolünü hariç tutar.
    @Override
    public List<RoleResponseDto> getAssignableRoles(Long companyId) {
        validateCompany(companyId);

        return roleRepository.findAllByCompanyIdAndActiveTrueOrderByNameAsc(companyId)
                .stream()
                .filter(role -> !COMPANY_ADMIN_ROLE_CODE.equals(role.getCode()))
                .map(roleMapper::toResponseDto)
                .toList();
    }

    // İşlem yapılan şirketin varlığını doğrular.
    private void validateCompany(Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException("Şirket bulunamadı: " + companyId);
        }
    }
}
