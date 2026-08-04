package com.example.elhabashyback.sector.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SectorDescriptionRequest(
        @NotBlank @Size(max = 2000) String ar,
        @NotBlank @Size(max = 2000) String en
) {
}
