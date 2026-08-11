package com.yasarbilgi.ats.contactlead.dto;
import com.yasarbilgi.ats.contactlead.entity.*;
import com.yasarbilgi.ats.interaction.entity.InteractionChannel;
import jakarta.validation.constraints.*;
public record ResolveContactLeadRequest(@NotNull ContactResolution resolution, @NotNull InteractionChannel channel, ContactRejectionReason rejectionReason, @Size(max=5000) String note) {}
