package com.yasarbilgi.ats.user.service.impl;

import com.yasarbilgi.ats.common.exception.BusinessRuleException;
import com.yasarbilgi.ats.common.exception.ResourceNotFoundException;
import com.yasarbilgi.ats.company.entity.Company;
import com.yasarbilgi.ats.company.repository.CompanyRepository;
import com.yasarbilgi.ats.department.entity.Department;
import com.yasarbilgi.ats.department.repository.DepartmentRepository;
import com.yasarbilgi.ats.role.entity.Role;
import com.yasarbilgi.ats.role.repository.RoleRepository;
import com.yasarbilgi.ats.user.dto.request.CreateUserRequestDto;
import com.yasarbilgi.ats.user.dto.request.UpdateUserRequestDto;
import com.yasarbilgi.ats.user.dto.request.UpdateUserRolesRequestDto;
import com.yasarbilgi.ats.user.dto.response.UserResponseDto;
import com.yasarbilgi.ats.user.entity.User;
import com.yasarbilgi.ats.user.entity.UserStatus;
import com.yasarbilgi.ats.user.mapper.UserMapper;
import com.yasarbilgi.ats.user.repository.UserRepository;
import com.yasarbilgi.ats.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final String COMPANY_ADMIN_ROLE_CODE = "COMPANY_ADMIN";

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    // Kullanıcıyı davet durumunda, seçilen departman ve rollerle oluşturur.
    @Override
    @Transactional
    public UserResponseDto create(Long companyId, CreateUserRequestDto request) {
        Company company = getCompany(companyId);
        String normalizedEmail = normalizeEmail(request.email());
        validateEmailIsAvailable(companyId, normalizedEmail, null);

        Department department = getDepartment(companyId, request.departmentId());
        Set<Role> roles = getAssignableRoles(companyId, request.roleIds());

        User user = User.builder()
                .company(company)
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(normalizedEmail)
                .department(department)
                .status(UserStatus.INVITED)
                .roles(roles)
                .build();

        return userMapper.toResponseDto(userRepository.save(user));
    }

    // Kullanıcıları tüm şirketten veya seçilen departmandan getirir.
    @Override
    public List<UserResponseDto> getAll(Long companyId, Long departmentId) {
        getCompany(companyId);

        List<User> users = departmentId == null
                ? userRepository.findAllByCompanyIdOrderByFirstNameAscLastNameAsc(companyId)
                : getUsersByDepartment(companyId, departmentId);

        return users.stream()
                .map(userMapper::toResponseDto)
                .toList();
    }

    // Kullanıcı detayını şirket sınırı içerisinde getirir.
    @Override
    public UserResponseDto getById(Long companyId, Long userId) {
        return userMapper.toResponseDto(getUser(companyId, userId));
    }

    // Kullanıcının ad, e-posta ve departman bilgisini günceller.
    @Override
    @Transactional
    public UserResponseDto update(Long companyId, Long userId, UpdateUserRequestDto request) {
        User user = getUser(companyId, userId);
        String normalizedEmail = normalizeEmail(request.email());
        validateEmailIsAvailable(companyId, normalizedEmail, userId);

        Department department = getDepartment(companyId, request.departmentId());
        user.updateProfile(
                request.firstName().trim(),
                request.lastName().trim(),
                normalizedEmail,
                department
        );

        return userMapper.toResponseDto(user);
    }

    // Kullanıcının atanabilir rollerini topluca günceller.
    @Override
    @Transactional
    public UserResponseDto updateRoles(
            Long companyId,
            Long userId,
            UpdateUserRolesRequestDto request
    ) {
        User user = getUser(companyId, userId);
        user.replaceRoles(getAssignableRoles(companyId, request.roleIds()));
        return userMapper.toResponseDto(user);
    }

    // Kullanıcının erişimini askıya alır ve kaydı pasifleştirir.
    @Override
    @Transactional
    public UserResponseDto deactivate(Long companyId, Long userId) {
        User user = getUser(companyId, userId);
        user.changeStatus(UserStatus.SUSPENDED);
        user.deactivate();
        return userMapper.toResponseDto(user);
    }

    // Kullanıcı kaydını yeniden aktifleştirir.
    @Override
    @Transactional
    public UserResponseDto activate(Long companyId, Long userId) {
        User user = getUser(companyId, userId);
        user.activate();

        if (user.getPasswordHash() == null) {
            user.changeStatus(UserStatus.INVITED);
        } else {
            user.changeStatus(UserStatus.ACTIVE);
        }

        return userMapper.toResponseDto(user);
    }

    // Departman filtresini doğruladıktan sonra o departmanın kullanıcılarını getirir.
    private List<User> getUsersByDepartment(Long companyId, Long departmentId) {
        getDepartment(companyId, departmentId);
        return userRepository.findAllByCompanyIdAndDepartmentIdOrderByFirstNameAscLastNameAsc(
                companyId,
                departmentId
        );
    }

    // Şirketi kimliğine göre getirir.
    private Company getCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Şirket bulunamadı: " + companyId
                ));
    }

    // Kullanıcıyı şirket sınırı içerisinde detaylarıyla getirir.
    private User getUser(Long companyId, Long userId) {
        return userRepository.findWithDetailsByCompanyIdAndId(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Kullanıcı bulunamadı: " + userId
                ));
    }

    // Departman seçilmişse departmanın aynı şirkete ait olduğunu doğrular.
    private Department getDepartment(Long companyId, Long departmentId) {
        if (departmentId == null) {
            return null;
        }

        return departmentRepository.findByCompanyIdAndId(companyId, departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Departman bulunamadı: " + departmentId
                ));
    }

    // Rol kimliklerinin tamamını doğrular ve korumalı şirket yöneticisi rolünü engeller.
    private Set<Role> getAssignableRoles(Long companyId, Set<Long> roleIds) {
        List<Role> roles = roleRepository.findAllByCompanyIdAndIdInAndActiveTrue(companyId, roleIds);

        if (roles.size() != roleIds.size()) {
            throw new BusinessRuleException(
                    "Seçilen rollerden biri bulunamadı veya bu şirkete ait değil."
            );
        }

        if (roles.stream().anyMatch(role -> COMPANY_ADMIN_ROLE_CODE.equals(role.getCode()))) {
            throw new BusinessRuleException(
                    "Şirket yöneticisi rolü yalnızca Super Admin tarafından atanabilir."
            );
        }

        return new HashSet<>(roles);
    }

    // E-posta adresini karşılaştırma ve kayıt için standart biçime getirir.
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    // E-posta adresinin şirket içindeki başka bir kullanıcı tarafından kullanılmadığını doğrular.
    private void validateEmailIsAvailable(Long companyId, String email, Long currentUserId) {
        userRepository.findByCompanyIdAndEmailIgnoreCase(companyId, email)
                .filter(existingUser -> !existingUser.getId().equals(currentUserId))
                .ifPresent(existingUser -> {
                    throw new BusinessRuleException(
                            "Bu e-posta adresi şirket içinde zaten kullanılıyor."
                    );
                });
    }
}
