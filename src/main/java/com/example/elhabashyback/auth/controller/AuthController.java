package com.example.elhabashyback.auth.controller;

import com.example.elhabashyback.auth.dto.AuthResponse;
import com.example.elhabashyback.auth.dto.LoginRequest;
import com.example.elhabashyback.auth.dto.EmailRequest;
import com.example.elhabashyback.auth.dto.MessageResponse;
import com.example.elhabashyback.auth.dto.RegisterRequest;
import com.example.elhabashyback.auth.dto.RegistrationResponse;
import com.example.elhabashyback.auth.dto.ResetPasswordRequest;
import com.example.elhabashyback.auth.service.AccountVerificationService;
import com.example.elhabashyback.auth.service.AuthService;
import com.example.elhabashyback.auth.service.PasswordResetService;
import com.example.elhabashyback.configuration.mail.AppMailProperties;
import com.example.elhabashyback.configuration.security.AppJwtProperties;
import com.example.elhabashyback.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AccountVerificationService accountVerificationService;
    private final PasswordResetService passwordResetService;
    private final AppJwtProperties properties;
    private final AppMailProperties mailProperties;

    @PostMapping("/register")
    ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return authResponse(authService.login(request), HttpStatus.OK);
    }

    @GetMapping(value = "/activate", produces = "text/html;charset=UTF-8")
    ResponseEntity<String> activate(@RequestParam String token) {
        accountVerificationService.activate(token);
        String frontendUrl = org.springframework.web.util.HtmlUtils.htmlEscape(mailProperties.frontendBaseUrl());
        String html = """
                <!doctype html><html lang="ar" dir="rtl"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                <body style="margin:0;background:#f1f5f9;font-family:Tahoma,Arial,sans-serif;display:grid;min-height:100vh;place-items:center">
                  <main style="max-width:520px;margin:20px;background:#fff;border-radius:28px;padding:38px;text-align:center;box-shadow:0 20px 60px rgba(15,23,42,.12)">
                    <div style="color:#d97706;font-weight:900">EL HABASHY</div>
                    <h1 style="color:#020617;font-size:30px">تم تفعيل حسابك بنجاح</h1>
                    <p style="color:#64748b;line-height:1.9">يمكنك الآن تسجيل الدخول ومتابعة العروض والمزادات.</p>
                    <a href="{{FRONTEND}}" style="display:inline-block;margin-top:16px;background:#fbbf24;color:#0f172a;text-decoration:none;font-weight:900;padding:14px 28px;border-radius:999px">الذهاب لتسجيل الدخول</a>
                  </main>
                </body></html>
                """.replace("{{FRONTEND}}", frontendUrl);
        return ResponseEntity.ok(html);
    }

    @PostMapping("/resend-activation")
    MessageResponse resendActivation(@Valid @RequestBody EmailRequest request) {
        accountVerificationService.resend(request.email());
        return new MessageResponse("If the account exists and is not active, an activation email will be sent.");
    }

    @PostMapping("/forgot-password")
    MessageResponse forgotPassword(@Valid @RequestBody EmailRequest request) {
        passwordResetService.requestReset(request.email());
        return new MessageResponse("If an active account exists, a password reset code will be sent.");
    }

    @PostMapping("/reset-password")
    MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.reset(request);
        return new MessageResponse("Password has been reset successfully.");
    }

    @PostMapping("/refresh")
    ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "${app.security.jwt.refresh-cookie-name}", required = false) String refreshToken
    ) {
        return authResponse(authService.refresh(refreshToken), HttpStatus.OK);
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(
            @CookieValue(name = "${app.security.jwt.refresh-cookie-name}", required = false) String refreshToken
    ) {
        authService.logout(refreshToken);
        ResponseCookie expiredCookie = ResponseCookie.from(properties.refreshCookieName(), "")
                .httpOnly(true)
                .secure(properties.refreshCookieSecure())
                .sameSite(properties.refreshCookieSameSite())
                .path("/api/v1/auth")
                .maxAge(0)
                .build();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .build();
    }

    @GetMapping("/me")
    UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return authService.currentUser(jwt.getSubject());
    }

    private ResponseEntity<AuthResponse> authResponse(AuthService.AuthSession session, HttpStatus status) {
        ResponseCookie refreshCookie = ResponseCookie.from(
                        properties.refreshCookieName(), session.refreshToken())
                .httpOnly(true)
                .secure(properties.refreshCookieSecure())
                .sameSite(properties.refreshCookieSameSite())
                .path("/api/v1/auth")
                .maxAge(properties.refreshTokenTtl())
                .build();

        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(session.response());
    }
}
