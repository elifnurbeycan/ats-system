package com.yasarbilgi.ats.contactlead.dto;
import jakarta.validation.constraints.*;
public record CreateContactLeadRequest(@NotBlank @Size(max=100) String firstName, @NotBlank @Size(max=100) String lastName, @Size(max=500) String linkedinUrl, @NotNull Long positionId, @NotNull Long pipelineId) {}
