package com.example.elhabashyback.sector.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record UpdateSectorRequest(
        @NotNull @Valid SectorTitleRequest title,
        @NotNull @Valid SectorDescriptionRequest description
) {
}
