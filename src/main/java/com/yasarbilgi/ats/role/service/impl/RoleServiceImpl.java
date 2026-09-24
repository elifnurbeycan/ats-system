package com.yasarbilgi.ats.role.service.impl;

import com.yasarbilgi.ats.common.exception.BusinessRuleException;
import com.yasarbilgi.ats.common.exception.ResourceNotFoundException;
import com.yasarbilgi.ats.company.repository.CompanyRepository;
import com.yasarbilgi.ats.permission.entity.Permission;
import com.yasarbilgi.ats.permission.entity.PermissionCode;
import com.yasarbilgi.ats.permission.repository.PermissionRepository;
import com.yasarbilgi.ats.role.dto.request.CreateRoleRequestDto;
import com.yasarbilgi.ats.role.dto.request.UpdateRoleRequestDto;
import com.yasarbilgi.ats.role.dto.response.PermissionResponseDto;
import com.yasarbilgi.ats.role.dto.response.RoleResponseDto;
import com.yasarbilgi.ats.role.entity.Role;
import com.yasarbilgi.ats.role.mapper.RoleMapper;
import com.yasarbilgi.ats.role.repository.RoleRepository;
import com.yasarbilgi.ats.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private static final String COMPANY_ADMIN_ROLE_CODE = "COMPANY_ADMIN";

    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
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

    @Override
    public List<PermissionResponseDto> getPermissions(Long companyId) {
        validateCompany(companyId);
        return permissionRepository.findAllByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(permission -> new PermissionResponseDto(permission.getId(), permission.getCode(),
                        permission.getName(), permission.getDescription(), permission.getCategory(),
                        permission.getDisplayOrder()))
                .toList();
    }

    @Override
    @Transactional
    public RoleResponseDto create(Long companyId, CreateRoleRequestDto request) {
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Şirket bulunamadı: " + companyId));
        String name = request.name().trim();
        if (roleRepository.existsByCompanyIdAndNameIgnoreCase(companyId, name)) {
            throw new BusinessRuleException("Bu isimde bir rol zaten bulunuyor.");
        }
        Role role = Role.builder().company(company).code(generateCode()).name(name)
                .description(normalizeDescription(request.description())).dataScope(request.dataScope())
                .systemRole(false).permissions(resolvePermissions(request.permissions())).build();
        return roleMapper.toResponseDto(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleResponseDto update(Long companyId, Long roleId, UpdateRoleRequestDto request) {
        Role role = findRole(companyId, roleId);
        ensureCustomRole(role);
        String name = request.name().trim();
        boolean duplicate = roleRepository.findAllByCompanyIdAndActiveTrueOrderByNameAsc(companyId).stream()
                .anyMatch(candidate -> !candidate.getId().equals(roleId) && candidate.getName().equalsIgnoreCase(name));
        if (duplicate) throw new BusinessRuleException("Bu isimde bir rol zaten bulunuyor.");
        role.updateDefinition(name, normalizeDescription(request.description()), request.dataScope(),
                resolvePermissions(request.permissions()));
        return roleMapper.toResponseDto(role);
    }

    @Override
    @Transactional
    public void deactivate(Long companyId, Long roleId) {
        Role role = findRole(companyId, roleId);
        ensureCustomRole(role);
        role.deactivate();
    }

    private Role findRole(Long companyId, Long roleId) {
        return roleRepository.findByCompanyIdAndId(companyId, roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol bulunamadı."));
    }

    private void ensureCustomRole(Role role) {
        if (role.isSystemRole()) throw new BusinessRuleException("Sistem rolü değiştirilemez.");
    }

    private Set<Permission> resolvePermissions(Set<PermissionCode> codes) {
        Set<Permission> permissions = codes.stream()
                .map(code -> permissionRepository.findByCode(code).filter(Permission::isActive)
                        .orElseThrow(() -> new BusinessRuleException("Geçersiz yetki: " + code)))
                .collect(Collectors.toSet());
        if (permissions.size() != codes.size()) throw new BusinessRuleException("Yetki listesi geçersiz.");
        return permissions;
    }

    private String generateCode() {
        return "CUSTOM_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
    }

    private String normalizeDescription(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }

    // İşlem yapılan şirketin varlığını doğrular.
    private void validateCompany(Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException("Şirket bulunamadı: " + companyId);
        }
    }
}
