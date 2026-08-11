package com.yasarbilgi.ats.position.service.impl;

import com.yasarbilgi.ats.common.exception.BusinessRuleException;
import com.yasarbilgi.ats.common.exception.ResourceNotFoundException;
import com.yasarbilgi.ats.company.entity.Company;
import com.yasarbilgi.ats.company.repository.CompanyRepository;
import com.yasarbilgi.ats.department.entity.Department;
import com.yasarbilgi.ats.department.repository.DepartmentRepository;
import com.yasarbilgi.ats.position.dto.request.ChangePositionStatusRequestDto;
import com.yasarbilgi.ats.position.dto.request.CreatePositionRequestDto;
import com.yasarbilgi.ats.position.dto.request.UpdatePositionRequestDto;
import com.yasarbilgi.ats.position.dto.response.PositionResponseDto;
import com.yasarbilgi.ats.position.dto.response.PositionSummaryResponseDto;
import com.yasarbilgi.ats.position.entity.Position;
import com.yasarbilgi.ats.position.entity.PositionStatus;
import com.yasarbilgi.ats.position.mapper.PositionMapper;
import com.yasarbilgi.ats.position.repository.PositionRepository;
import com.yasarbilgi.ats.position.service.PositionService;
import com.yasarbilgi.ats.security.service.DataScopeService;
import com.yasarbilgi.ats.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PositionServiceImpl implements PositionService {

    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final PositionMapper positionMapper;
    private final DataScopeService dataScopeService;

    // Pozisyonu taslak durumda ve normalize edilmiş benzersiz koduyla oluşturur.
    @Override
    @Transactional
    public PositionResponseDto create(Long companyId, CreatePositionRequestDto request) {
        Company company = getCompany(companyId);
        Department department = getActiveDepartment(companyId, request.departmentId());
        dataScopeService.requireDepartmentAccess(department.getId());
        String normalizedCode = normalizeCode(request.code());

        if (positionRepository.existsByCompanyIdAndCode(companyId, normalizedCode)) {
            throw new BusinessRuleException(
                    "Bu pozisyon kodu şirket içinde zaten kullanılıyor."
            );
        }

        Position position = Position.builder()
                .company(company)
                .department(department)
                .title(request.title().trim())
                .code(normalizedCode)
                .description(normalizeDescription(request.description()))
                .vacancyCount(request.vacancyCount())
                .status(PositionStatus.DRAFT)
                .build();

        return positionMapper.toResponseDto(positionRepository.save(position));
    }

    // Pozisyonları isteğe bağlı departman ve durum filtreleriyle getirir.
    @Override
    public PageResponse<PositionResponseDto> getAll(
            Long companyId,
            Long departmentId,
            PositionStatus status,
            int page,
            int size
    ) {
        getCompany(companyId);

        if (departmentId != null) {
            getDepartment(companyId, departmentId);
            dataScopeService.requireDepartmentAccess(departmentId);
        }

        validatePageRequest(page, size);
        PageRequest pageable = PageRequest.of(page, size, Sort.by("title").ascending());
        var result = dataScopeService.hasCompanyScope()
                ? positionRepository.search(companyId, departmentId, status, pageable)
                : positionRepository.searchByDepartmentScope(companyId,
                        requireManagedDepartments(), departmentId, status, pageable);
        return PageResponse.from(result, positionMapper::toResponseDto);
    }

    // Pozisyon detayını şirket sınırı içerisinde getirir.
    @Override
    public PositionResponseDto getById(Long companyId, Long positionId) {
        Position position = getPosition(companyId, positionId);
        dataScopeService.requireDepartmentAccess(position.getDepartment().getId());
        return positionMapper.toResponseDto(position);
    }

    // Kapanmamış pozisyonun temel bilgilerini günceller.
    @Override
    @Transactional
    public PositionResponseDto update(
            Long companyId,
            Long positionId,
            UpdatePositionRequestDto request
    ) {
        Position position = getPosition(companyId, positionId);
        dataScopeService.requireDepartmentAccess(position.getDepartment().getId());
        validateEditable(position);
        Department department = getActiveDepartment(companyId, request.departmentId());
        dataScopeService.requireDepartmentAccess(department.getId());

        position.updateDetails(
                request.title().trim(),
                normalizeDescription(request.description()),
                request.vacancyCount(),
                department
        );

        return positionMapper.toResponseDto(position);
    }

    // Pozisyon durumunu yalnızca tanımlanmış geçiş kurallarına göre değiştirir.
    @Override
    @Transactional
    public PositionResponseDto changeStatus(
            Long companyId,
            Long positionId,
            ChangePositionStatusRequestDto request
    ) {
        Position position = getPosition(companyId, positionId);
        dataScopeService.requireDepartmentAccess(position.getDepartment().getId());
        PositionStatus targetStatus = request.status();

        if (position.getStatus() == targetStatus) {
            return positionMapper.toResponseDto(position);
        }

        validateStatusTransition(position.getStatus(), targetStatus);
        applyStatus(position, targetStatus);
        return positionMapper.toResponseDto(position);
    }

    // Aday ekleme ekranında kullanılacak açık pozisyonları getirir.
    @Override
    public List<PositionSummaryResponseDto> getOpenPositions(Long companyId) {
        getCompany(companyId);

        var managedDepartmentIds = dataScopeService.getManagedDepartmentIds();

        return positionRepository
                .findAllByCompanyIdAndStatusAndActiveTrueOrderByTitleAsc(
                        companyId,
                        PositionStatus.OPEN
                )
                .stream()
                .filter(position -> dataScopeService.hasCompanyScope()
                        || managedDepartmentIds.contains(position.getDepartment().getId()))
                .map(positionMapper::toSummaryResponseDto)
                .toList();
    }

    // Hedef durumun mevcut pozisyondan erişilebilir olup olmadığını doğrular.
    private void validateStatusTransition(PositionStatus current, PositionStatus target) {
        boolean allowed = switch (current) {
            case DRAFT -> target == PositionStatus.OPEN
                    || target == PositionStatus.CANCELLED;
            case OPEN -> target == PositionStatus.ON_HOLD
                    || target == PositionStatus.CLOSED
                    || target == PositionStatus.CANCELLED;
            case ON_HOLD -> target == PositionStatus.OPEN
                    || target == PositionStatus.CLOSED
                    || target == PositionStatus.CANCELLED;
            case CLOSED, CANCELLED -> false;
        };

        if (!allowed) {
            throw new BusinessRuleException(
                    current + " durumundan " + target + " durumuna geçilemez."
            );
        }
    }

    // Doğrulanmış hedef durumu ilgili entity davranışı üzerinden uygular.
    private void applyStatus(Position position, PositionStatus targetStatus) {
        switch (targetStatus) {
            case OPEN -> position.open();
            case ON_HOLD -> position.putOnHold();
            case CLOSED -> position.close();
            case CANCELLED -> position.cancel();
            case DRAFT -> throw new BusinessRuleException(
                    "Pozisyon sonradan taslak durumuna döndürülemez."
            );
        }
    }

    // Kapanmış veya iptal edilmiş pozisyonların değiştirilmesini engeller.
    private void validateEditable(Position position) {
        if (position.getStatus() == PositionStatus.CLOSED
                || position.getStatus() == PositionStatus.CANCELLED) {
            throw new BusinessRuleException(
                    "Kapanmış veya iptal edilmiş pozisyon güncellenemez."
            );
        }
    }

    // İşlem yapılan şirketi kimliğine göre getirir.
    private Company getCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Şirket bulunamadı: " + companyId
                ));
    }

    // Pozisyonu şirket sınırı içerisinde getirir.
    private Position getPosition(Long companyId, Long positionId) {
        return positionRepository.findByCompanyIdAndId(companyId, positionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pozisyon bulunamadı: " + positionId
                ));
    }

    // Departmanı şirket sınırı içerisinde getirir.
    private Department getDepartment(Long companyId, Long departmentId) {
        return departmentRepository.findByCompanyIdAndId(companyId, departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Departman bulunamadı: " + departmentId
                ));
    }

    // Pozisyon için yalnızca aktif departman seçilmesini sağlar.
    private Department getActiveDepartment(Long companyId, Long departmentId) {
        Department department = getDepartment(companyId, departmentId);

        if (!department.isActive()) {
            throw new BusinessRuleException("Pasif departmana pozisyon atanamaz.");
        }

        return department;
    }

    // Pozisyon kodunu boşluksuz ve büyük harfli standart biçime getirir.
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

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new BusinessRuleException("Geçersiz sayfalama bilgisi.");
    }

    private java.util.Set<Long> requireManagedDepartments() {
        java.util.Set<Long> ids = dataScopeService.getManagedDepartmentIds();
        if (ids.isEmpty()) throw new com.yasarbilgi.ats.common.exception.ForbiddenException(
                "Yönetilen aktif bir departman bulunmuyor.");
        return ids;
    }
}
