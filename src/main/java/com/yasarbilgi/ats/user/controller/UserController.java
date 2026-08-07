package com.yasarbilgi.ats.user.controller;

import com.yasarbilgi.ats.common.response.ApiResponse;
import com.yasarbilgi.ats.user.dto.request.CreateUserRequestDto;
import com.yasarbilgi.ats.user.dto.request.UpdateUserRequestDto;
import com.yasarbilgi.ats.user.dto.request.UpdateUserRolesRequestDto;
import com.yasarbilgi.ats.user.dto.response.UserResponseDto;
import com.yasarbilgi.ats.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import com.yasarbilgi.ats.common.response.PageResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/companies/{companyId}/users")
public class UserController {

    private final UserService userService;

    // Şirket için yeni bir kullanıcı davet kaydı oluşturur.
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDto>> create(
            @PathVariable Long companyId,
            @Valid @RequestBody CreateUserRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Kullanıcı davet kaydı oluşturuldu.",
                        userService.create(companyId, request)
                ));
    }

    // Şirket kullanıcılarını isteğe bağlı departman filtresiyle listeler.
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponseDto>>> getAll(
            @PathVariable Long companyId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getAll(companyId, departmentId, page, size)
        ));
    }

    // Şirkete ait kullanıcı detayını getirir.
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getById(
            @PathVariable Long companyId,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getById(companyId, userId)
        ));
    }

    // Kullanıcının temel profil ve departman bilgilerini günceller.
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> update(
            @PathVariable Long companyId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequestDto request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Kullanıcı bilgileri güncellendi.",
                userService.update(companyId, userId, request)
        ));
    }

    // Kullanıcının rollerini verilen rol kümesiyle değiştirir.
    @PutMapping("/{userId}/roles")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateRoles(
            @PathVariable Long companyId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRolesRequestDto request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Kullanıcı rolleri güncellendi.",
                userService.updateRoles(companyId, userId, request)
        ));
    }

    // Kullanıcı hesabını fiziksel olarak silmeden pasifleştirir.
    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<ApiResponse<UserResponseDto>> deactivate(
            @PathVariable Long companyId,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Kullanıcı silindi.",
                userService.deactivate(companyId, userId)
        ));
    }

    // Pasif kullanıcı hesabını yeniden aktifleştirir.
    @PatchMapping("/{userId}/activate")
    public ResponseEntity<ApiResponse<UserResponseDto>> activate(
            @PathVariable Long companyId,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Kullanıcı aktifleştirildi.",
                userService.activate(companyId, userId)
        ));
    }
}
