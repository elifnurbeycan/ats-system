package com.yasarbilgi.ats.role.controller;

import com.yasarbilgi.ats.common.response.ApiResponse;
import com.yasarbilgi.ats.role.dto.response.RoleResponseDto;
import com.yasarbilgi.ats.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
