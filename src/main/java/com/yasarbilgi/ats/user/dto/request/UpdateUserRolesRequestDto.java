package com.yasarbilgi.ats.user.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateUserRolesRequestDto(
        @NotEmpty Set<Long> roleIds
) {
}
