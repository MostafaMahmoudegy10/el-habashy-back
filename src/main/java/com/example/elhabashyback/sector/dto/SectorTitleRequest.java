package com.example.elhabashyback.sector.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SectorTitleRequest(
        @NotBlank @Size(max = 255) String ar,
        @NotBlank @Size(max = 255) String en
) {
}
