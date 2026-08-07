package com.yasarbilgi.ats.user.service;

import com.yasarbilgi.ats.user.dto.request.CreateUserRequestDto;
import com.yasarbilgi.ats.user.dto.request.UpdateUserRequestDto;
import com.yasarbilgi.ats.user.dto.request.UpdateUserRolesRequestDto;
import com.yasarbilgi.ats.user.dto.response.UserResponseDto;

import java.util.List;
import com.yasarbilgi.ats.common.response.PageResponse;

public interface UserService {

    // Şirkete yeni bir kullanıcı davet kaydı oluşturur.
    UserResponseDto create(Long companyId, CreateUserRequestDto request);

    // Şirkete ait kullanıcıları isteğe bağlı departman filtresiyle listeler.
    PageResponse<UserResponseDto> getAll(Long companyId, Long departmentId, int page, int size);

    // Şirkete ait tek bir kullanıcının detayını getirir.
    UserResponseDto getById(Long companyId, Long userId);

    // Kullanıcının temel profil ve departman bilgilerini günceller.
    UserResponseDto update(Long companyId, Long userId, UpdateUserRequestDto request);

    // Kullanıcının rollerini yeni rol kümesiyle değiştirir.
    UserResponseDto updateRoles(Long companyId, Long userId, UpdateUserRolesRequestDto request);

    // Kullanıcıyı fiziksel olarak silmeden pasifleştirir.
    UserResponseDto deactivate(Long companyId, Long userId);

    // Pasif kullanıcıyı yeniden aktif hâle getirir.
    UserResponseDto activate(Long companyId, Long userId);
}
