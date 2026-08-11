package com.yasarbilgi.ats.department.service.impl;

import com.yasarbilgi.ats.common.exception.BusinessRuleException;
import com.yasarbilgi.ats.common.exception.ResourceNotFoundException;
import com.yasarbilgi.ats.department.dto.request.AssignDepartmentManagerRequestDto;
import com.yasarbilgi.ats.department.dto.response.DepartmentManagerAssignmentResponseDto;
import com.yasarbilgi.ats.department.entity.Department;
import com.yasarbilgi.ats.department.entity.DepartmentManagerAssignment;
import com.yasarbilgi.ats.department.mapper.DepartmentManagerAssignmentMapper;
import com.yasarbilgi.ats.department.repository.DepartmentManagerAssignmentRepository;
import com.yasarbilgi.ats.department.repository.DepartmentRepository;
import com.yasarbilgi.ats.department.service.DepartmentManagerAssignmentService;
import com.yasarbilgi.ats.common.response.PageResponse;
import com.yasarbilgi.ats.user.entity.User;
import com.yasarbilgi.ats.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentManagerAssignmentServiceImpl
        implements DepartmentManagerAssignmentService {

    private static final String DEPARTMENT_MANAGER_ROLE_CODE = "DEPARTMENT_MANAGER";

    private final DepartmentRepository departmentRepository;
    private final DepartmentManagerAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final DepartmentManagerAssignmentMapper assignmentMapper;

    // Kullanıcıyı rol ve tekrar atama kurallarını doğrulayarak departmana yönetici atar.
    @Override
    @Transactional
    public DepartmentManagerAssignmentResponseDto assign(
            Long companyId,
            Long departmentId,
            AssignDepartmentManagerRequestDto request
    ) {
        Department department = getActiveDepartment(companyId, departmentId);
        User user = getActiveUser(companyId, request.userId());
        validateDepartmentManagerRole(user);

        if (assignmentRepository.existsByCompanyIdAndDepartmentIdAndUserIdAndActiveTrue(
                companyId,
                departmentId,
                user.getId()
        )) {
            throw new BusinessRuleException(
                    "Kullanıcı bu departmanda zaten aktif yönetici olarak atanmış."
            );
        }

        DepartmentManagerAssignment assignment = DepartmentManagerAssignment.builder()
                .company(department.getCompany())
                .department(department)
                .user(user)
                .startedAt(Instant.now())
                .build();

        return assignmentMapper.toResponseDto(assignmentRepository.save(assignment));
    }

    // Departmanın aktif veya geçmiş dahil tüm yönetici atamalarını getirir.
    @Override
    public PageResponse<DepartmentManagerAssignmentResponseDto> getAll(
            Long companyId,
            Long departmentId,
            boolean includeHistory,
            int page,
            int size
    ) {
        getDepartment(companyId, departmentId);
        if (page < 0 || size < 1 || size > 100) throw new BusinessRuleException("Geçersiz sayfalama bilgisi.");

        PageRequest pageable = PageRequest.of(page, size, Sort.by("startedAt").descending());
        var assignments = includeHistory
                ? assignmentRepository.findAllByCompanyIdAndDepartmentId(companyId, departmentId, pageable)
                : assignmentRepository.findAllByCompanyIdAndDepartmentIdAndActiveTrue(companyId, departmentId, pageable);
        return PageResponse.from(assignments, assignmentMapper::toResponseDto);
    }

    // Aktif yönetici atamasını bitiş zamanı kaydederek sona erdirir.
    @Override
    @Transactional
    public DepartmentManagerAssignmentResponseDto endAssignment(
            Long companyId,
            Long departmentId,
            Long assignmentId
    ) {
        DepartmentManagerAssignment assignment = assignmentRepository
                .findWithDetailsByCompanyIdAndDepartmentIdAndId(
                        companyId,
                        departmentId,
                        assignmentId
                )
                .filter(DepartmentManagerAssignment::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aktif departman yöneticisi ataması bulunamadı."
                ));

        assignment.endAssignment();
        return assignmentMapper.toResponseDto(assignment);
    }

    // Departmanı şirket sınırı içerisinde getirir.
    private Department getDepartment(Long companyId, Long departmentId) {
        return departmentRepository.findByCompanyIdAndId(companyId, departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Departman bulunamadı: " + departmentId
                ));
    }

    // Atama yapılacak departmanın aktif olduğunu doğrular.
    private Department getActiveDepartment(Long companyId, Long departmentId) {
        Department department = getDepartment(companyId, departmentId);
        if (!department.isActive()) {
            throw new BusinessRuleException("Pasif departmana yönetici atanamaz.");
        }
        return department;
    }

    // Kullanıcıyı şirket sınırında rolleriyle getirip aktif olduğunu doğrular.
    private User getActiveUser(Long companyId, Long userId) {
        return userRepository.findWithDetailsByCompanyIdAndId(companyId, userId)
                .filter(User::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aktif kullanıcı bulunamadı: " + userId
                ));
    }

    // Kullanıcının departman yöneticisi rolüne sahip olduğunu doğrular.
    private void validateDepartmentManagerRole(User user) {
        boolean hasRole = user.getRoles().stream()
                .anyMatch(role -> DEPARTMENT_MANAGER_ROLE_CODE.equals(role.getCode()));

        if (!hasRole) {
            throw new BusinessRuleException(
                    "Kullanıcıya önce Departman Yöneticisi rolü atanmalıdır."
            );
        }
    }
}
