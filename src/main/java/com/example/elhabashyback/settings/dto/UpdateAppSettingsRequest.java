package com.example.elhabashyback.settings.dto;

import com.example.elhabashyback.common.dto.LocalizedTextRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateAppSettingsRequest(
        @NotBlank
        @Pattern(regexp = "^\\+?[0-9][0-9\\s-]{7,38}$", message = "whatsappNumber must be a valid phone number")
        String whatsappNumber,
        @NotBlank @Size(max = 2000) String whatsappMessageAr,
        @NotBlank @Size(max = 2000) String whatsappMessageEn,
        @NotBlank @Size(max = 255) String contactPhone,
        @NotBlank @Email @Size(max = 320) String contactEmail,
        @NotNull @Valid LocalizedTextRequest officeAddress,
        @NotBlank @Size(max = 4096) String mapUrl,
        @Size(max = 4096) String facebookUrl,
        @Size(max = 4096) String linkedinUrl
) {
}
