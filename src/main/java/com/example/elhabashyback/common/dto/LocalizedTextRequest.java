package com.example.elhabashyback.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LocalizedTextRequest(
        @NotBlank @Size(max = 20000) String ar,
        @NotBlank @Size(max = 20000) String en
) {
}
