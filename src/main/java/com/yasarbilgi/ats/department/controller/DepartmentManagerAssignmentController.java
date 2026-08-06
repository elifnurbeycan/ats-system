package com.yasarbilgi.ats.department.controller;

import com.yasarbilgi.ats.common.response.ApiResponse;
import com.yasarbilgi.ats.department.dto.request.AssignDepartmentManagerRequestDto;
import com.yasarbilgi.ats.department.dto.response.DepartmentManagerAssignmentResponseDto;
import com.yasarbilgi.ats.department.service.DepartmentManagerAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/companies/{companyId}/departments/{departmentId}/managers")
public class DepartmentManagerAssignmentController {

    private final DepartmentManagerAssignmentService assignmentService;

    // Kullanıcıyı departmana yönetici olarak atar.
    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentManagerAssignmentResponseDto>> assign(
            @PathVariable Long companyId,
            @PathVariable Long departmentId,
            @Valid @RequestBody AssignDepartmentManagerRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Departman yöneticisi atandı.",
                        assignmentService.assign(companyId, departmentId, request)
                ));
    }

    // Departmanın yönetici atamalarını isteğe bağlı geçmiş bilgisiyle listeler.
    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentManagerAssignmentResponseDto>>> getAll(
            @PathVariable Long companyId,
            @PathVariable Long departmentId,
            @RequestParam(defaultValue = "false") boolean includeHistory
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                assignmentService.getAll(companyId, departmentId, includeHistory)
        ));
    }

    // Aktif yönetici atamasını tarihçeyi koruyarak sona erdirir.
    @PatchMapping("/{assignmentId}/end")
    public ResponseEntity<ApiResponse<DepartmentManagerAssignmentResponseDto>> endAssignment(
            @PathVariable Long companyId,
            @PathVariable Long departmentId,
            @PathVariable Long assignmentId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Departman yöneticisi ataması sona erdirildi.",
                assignmentService.endAssignment(companyId, departmentId, assignmentId)
        ));
    }
}
