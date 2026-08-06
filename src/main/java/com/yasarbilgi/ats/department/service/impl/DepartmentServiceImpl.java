package com.yasarbilgi.ats.department.service.impl;

import com.yasarbilgi.ats.common.exception.BusinessRuleException;
import com.yasarbilgi.ats.common.exception.ResourceNotFoundException;
import com.yasarbilgi.ats.company.entity.Company;
import com.yasarbilgi.ats.company.repository.CompanyRepository;
import com.yasarbilgi.ats.department.dto.request.CreateDepartmentRequestDto;
import com.yasarbilgi.ats.department.dto.request.UpdateDepartmentRequestDto;
import com.yasarbilgi.ats.department.dto.response.DepartmentResponseDto;
import com.yasarbilgi.ats.department.entity.Department;
import com.yasarbilgi.ats.department.mapper.DepartmentMapper;
import com.yasarbilgi.ats.department.repository.DepartmentRepository;
import com.yasarbilgi.ats.department.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    // Yeni departmanı normalize edilmiş benzersiz koduyla kaydeder.
    @Override
    @Transactional
    public DepartmentResponseDto create(Long companyId, CreateDepartmentRequestDto request) {
        Company company = getCompany(companyId);
        String normalizedCode = normalizeCode(request.code());

        if (departmentRepository.existsByCompanyIdAndCode(companyId, normalizedCode)) {
            throw new BusinessRuleException(
                    "Bu departman kodu şirket içinde zaten kullanılıyor."
            );
        }

        Department department = Department.builder()
                .company(company)
                .name(request.name().trim())
                .code(normalizedCode)
                .description(normalizeDescription(request.description()))
                .build();

        return departmentMapper.toResponseDto(departmentRepository.save(department));
    }

    // Departmanları yalnızca aktif veya tüm kayıtlar olacak şekilde listeler.
    @Override
    public List<DepartmentResponseDto> getAll(Long companyId, boolean includeInactive) {
        getCompany(companyId);

        List<Department> departments = includeInactive
                ? departmentRepository.findAllByCompanyIdOrderByNameAsc(companyId)
                : departmentRepository.findAllByCompanyIdAndActiveTrueOrderByNameAsc(companyId);

        return departments.stream()
                .map(departmentMapper::toResponseDto)
                .toList();
    }

    // Departmanı şirket sınırı içinde kimliğine göre getirir.
    @Override
    public DepartmentResponseDto getById(Long companyId, Long departmentId) {
        return departmentMapper.toResponseDto(getDepartment(companyId, departmentId));
    }

    // Departmanın düzenlenebilir bilgilerini günceller.
    @Override
    @Transactional
    public DepartmentResponseDto update(
            Long companyId,
            Long departmentId,
            UpdateDepartmentRequestDto request
    ) {
        Department department = getDepartment(companyId, departmentId);
        department.update(
                request.name().trim(),
                normalizeDescription(request.description())
        );

        return departmentMapper.toResponseDto(department);
    }

    // Departmanı fiziksel olarak silmeden pasifleştirir.
    @Override
    @Transactional
    public DepartmentResponseDto deactivate(Long companyId, Long departmentId) {
        Department department = getDepartment(companyId, departmentId);
        department.deactivate();
        return departmentMapper.toResponseDto(department);
    }

    // Pasif departmanı yeniden aktif hâle getirir.
    @Override
    @Transactional
    public DepartmentResponseDto activate(Long companyId, Long departmentId) {
        Department department = getDepartment(companyId, departmentId);
        department.activate();
        return departmentMapper.toResponseDto(department);
    }

    // İşlem yapılan şirketi kimliğine göre getirir.
    private Company getCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Şirket bulunamadı: " + companyId
                ));
    }

    // Departmanı şirket sınırı içerisinde getirir.
    private Department getDepartment(Long companyId, Long departmentId) {
        return departmentRepository.findByCompanyIdAndId(companyId, departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Departman bulunamadı: " + departmentId
                ));
    }

    // Departman kodunu boşluksuz ve büyük harfli standart biçime getirir.
    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    // Boş açıklamaları null değerine dönüştürerek veriyi sade tutar.
    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }
}
