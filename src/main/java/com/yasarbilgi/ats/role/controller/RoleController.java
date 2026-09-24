package com.yasarbilgi.ats.role.controller;

import com.yasarbilgi.ats.common.response.ApiResponse;
import com.yasarbilgi.ats.role.dto.response.RoleResponseDto;
import com.yasarbilgi.ats.role.dto.response.PermissionResponseDto;
import com.yasarbilgi.ats.role.dto.request.CreateRoleRequestDto;
import com.yasarbilgi.ats.role.dto.request.UpdateRoleRequestDto;
import com.yasarbilgi.ats.role.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/companies/{companyId}/roles")
public class RoleController {

    private final RoleService roleService;

    // Kullanıcı oluşturma ekranında seçilebilecek rolleri listeler.
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponseDto>>> getAssignableRoles(
            @PathVariable Long companyId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                roleService.getAssignableRoles(companyId)
        ));
    }

    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<List<PermissionResponseDto>>> getPermissions(@PathVariable Long companyId) {
        return ResponseEntity.ok(ApiResponse.success(roleService.getPermissions(companyId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponseDto>> create(
            @PathVariable Long companyId, @Valid @RequestBody CreateRoleRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Rol oluşturuldu.", roleService.create(companyId, request)));
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<ApiResponse<RoleResponseDto>> update(
            @PathVariable Long companyId, @PathVariable Long roleId,
            @Valid @RequestBody UpdateRoleRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Rol güncellendi.", roleService.update(companyId, roleId, request)));
    }

    @PatchMapping("/{roleId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long companyId, @PathVariable Long roleId) {
        roleService.deactivate(companyId, roleId);
        return ResponseEntity.ok(ApiResponse.success("Rol pasifleştirildi.", null));
    }
}
