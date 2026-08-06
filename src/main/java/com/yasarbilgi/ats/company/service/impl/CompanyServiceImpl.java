package com.yasarbilgi.ats.company.service.impl;

import com.yasarbilgi.ats.common.exception.BusinessRuleException;
import com.yasarbilgi.ats.common.exception.ResourceNotFoundException;
import com.yasarbilgi.ats.company.dto.request.*;
import com.yasarbilgi.ats.company.dto.response.*;
import com.yasarbilgi.ats.company.entity.*;
import com.yasarbilgi.ats.company.repository.CompanyRepository;
import com.yasarbilgi.ats.company.service.CompanyService;
import com.yasarbilgi.ats.permission.entity.*;
import com.yasarbilgi.ats.permission.repository.PermissionRepository;
import com.yasarbilgi.ats.role.entity.*;
import com.yasarbilgi.ats.role.repository.RoleRepository;
import com.yasarbilgi.ats.user.entity.*;
import com.yasarbilgi.ats.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyServiceImpl implements CompanyService {

    private static final Set<PermissionCode> READ_PERMISSIONS = EnumSet.of(
            PermissionCode.DEPARTMENT_VIEW, PermissionCode.POSITION_VIEW,
            PermissionCode.CANDIDATE_VIEW, PermissionCode.CANDIDATE_PROCESS_VIEW,
            PermissionCode.CANDIDATE_COMPENSATION_VIEW, PermissionCode.INTERVIEW_VIEW,
            PermissionCode.PIPELINE_VIEW);
    private static final Set<PermissionCode> INTERVIEWER_PERMISSIONS = EnumSet.of(
            PermissionCode.CANDIDATE_VIEW, PermissionCode.CANDIDATE_PROCESS_VIEW,
            PermissionCode.INTERVIEW_VIEW, PermissionCode.INTERVIEW_EVALUATE);
    private static final Set<PermissionCode> HR_PERMISSIONS = EnumSet.complementOf(
            EnumSet.of(PermissionCode.PIPELINE_MANAGE));

    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Yeni şirketi varsayılan roller, yetkiler, Company Admin ve İK kullanıcısıyla birlikte kurar.
    @Override
    @Transactional
    public CreatedCompanyResponseDto create(CreateCompanyRequestDto request) {
        String code = request.code().trim().toUpperCase(Locale.ROOT);
        String adminEmail = normalizeEmail(request.companyAdmin().email());
        String hrEmail = normalizeEmail(request.hrUser().email());
        if (companyRepository.existsByCode(code)) {
            throw new BusinessRuleException("Şirket kodu daha önce kullanılmış.");
        }
        if (adminEmail.equals(hrEmail)) {
            throw new BusinessRuleException("Company Admin ve İK kullanıcılarının e-postaları farklı olmalıdır.");
        }

        Company company = companyRepository.save(Company.builder()
                .name(request.name().trim()).code(code).status(CompanyStatus.ACTIVE).build());
        Map<String, Role> roles = createDefaultRoles(company);
        User admin = createInitialUser(company, request.companyAdmin(), adminEmail, roles.get("COMPANY_ADMIN"));
        User hr = createInitialUser(company, request.hrUser(), hrEmail, roles.get("HR"));
        return new CreatedCompanyResponseDto(toResponse(company), toInitialUser(admin, "COMPANY_ADMIN"),
                toInitialUser(hr, "HR"));
    }

    // Tüm şirketleri yönetim ekranında gösterilmek üzere getirir.
    @Override
    public List<CompanyResponseDto> getAll() {
        return companyRepository.findAllByOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    // Kimliği verilen şirketi getirir.
    @Override
    public CompanyResponseDto getById(Long companyId) {
        return toResponse(findCompany(companyId));
    }

    // Şirketin görünen adını günceller.
    @Override
    @Transactional
    public CompanyResponseDto update(Long companyId, UpdateCompanyRequestDto request) {
        Company company = findCompany(companyId);
        company.updateName(request.name().trim());
        return toResponse(company);
    }

    // Şirketi aktif, askıda veya kullanım dışı duruma geçirir.
    @Override
    @Transactional
    public CompanyResponseDto changeStatus(Long companyId, ChangeCompanyStatusRequestDto request) {
        Company company = findCompany(companyId);
        company.updateStatus(request.status());
        if (request.status() == CompanyStatus.ACTIVE) company.activate();
        if (request.status() == CompanyStatus.INACTIVE) company.deactivate();
        return toResponse(company);
    }

    // Yeni şirkete ait sistem rollerini ve rol yetkilerini oluşturur.
    private Map<String, Role> createDefaultRoles(Company company) {
        Map<PermissionCode, Permission> permissions = permissionRepository
                .findAllByActiveTrueOrderByDisplayOrderAsc().stream()
                .collect(Collectors.toMap(Permission::getCode, Function.identity()));
        if (permissions.size() < PermissionCode.values().length) {
            throw new BusinessRuleException("Sistem permission kayıtları eksik olduğu için şirket oluşturulamadı.");
        }
        List<RoleDefinition> definitions = List.of(
                new RoleDefinition("COMPANY_ADMIN", "Şirket Yöneticisi", DataScope.COMPANY, READ_PERMISSIONS),
                new RoleDefinition("HR", "İnsan Kaynakları", DataScope.COMPANY, HR_PERMISSIONS),
                new RoleDefinition("GENERAL_MANAGER", "Genel Müdür", DataScope.COMPANY, READ_PERMISSIONS),
                new RoleDefinition("DEPARTMENT_MANAGER", "Departman Yöneticisi", DataScope.DEPARTMENT, READ_PERMISSIONS),
                new RoleDefinition("INTERVIEWER", "Görüşmeci", DataScope.ASSIGNED, INTERVIEWER_PERMISSIONS));
        Map<String, Role> roles = new HashMap<>();
        for (RoleDefinition definition : definitions) {
            Role role = Role.builder().company(company).code(definition.code()).name(definition.name())
                    .description(definition.name() + " sistem rolü.").dataScope(definition.scope()).build();
            definition.permissions().forEach(code -> role.assignPermission(permissions.get(code)));
            roles.put(definition.code(), roleRepository.save(role));
        }
        return roles;
    }

    // İlk şirket kullanıcısını geçici şifresi ve sistem rolüyle oluşturur.
    private User createInitialUser(Company company, InitialCompanyUserRequestDto request,
                                   String email, Role role) {
        return userRepository.save(User.builder().company(company).firstName(request.firstName().trim())
                .lastName(request.lastName().trim()).email(email)
                .passwordHash(passwordEncoder.encode(request.temporaryPassword()))
                .status(UserStatus.ACTIVE).roles(new HashSet<>(Set.of(role))).build());
    }

    // Şirket kimliğini doğrular ve kaydı getirir.
    private Company findCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Şirket bulunamadı."));
    }

    // Şirket entity'sini API yanıtına dönüştürür.
    private CompanyResponseDto toResponse(Company company) {
        return new CompanyResponseDto(company.getId(), company.getName(), company.getCode(),
                company.getStatus(), company.isActive());
    }

    // İlk kullanıcı entity'sini güvenli API yanıtına dönüştürür.
    private CreatedCompanyResponseDto.InitialUser toInitialUser(User user, String roleCode) {
        return new CreatedCompanyResponseDto.InitialUser(user.getId(), user.getFullName(), user.getEmail(), roleCode);
    }

    // E-posta adresini karşılaştırma ve kayıt için standart biçime dönüştürür.
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private record RoleDefinition(String code, String name, DataScope scope, Set<PermissionCode> permissions) {}
}
