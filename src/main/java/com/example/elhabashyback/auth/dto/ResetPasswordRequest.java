package com.example.elhabashyback.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Pattern(regexp = "\\d{6}", message = "OTP must contain exactly 6 digits") String otp,
        @NotBlank @Size(min = 8, max = 72) String newPassword
) {
}
