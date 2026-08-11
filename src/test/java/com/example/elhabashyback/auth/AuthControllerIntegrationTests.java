package com.example.elhabashyback.auth;

import com.example.elhabashyback.auth.repository.RefreshTokenRepository;
import com.example.elhabashyback.auth.repository.EmailVerificationTokenRepository;
import com.example.elhabashyback.auth.repository.PasswordResetOtpRepository;
import com.example.elhabashyback.mail.ElHabashyMailService;
import com.example.elhabashyback.user.entity.Role;
import com.example.elhabashyback.user.repoistory.UserRepository;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import org.mockito.ArgumentCaptor;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    private PasswordResetOtpRepository passwordResetOtpRepository;

    @MockitoBean
    private ElHabashyMailService mailService;

    @BeforeEach
    void cleanDatabase() {
        refreshTokenRepository.deleteAll();
        emailVerificationTokenRepository.deleteAll();
        passwordResetOtpRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerRefreshMeAndRoleAuthorizationWork() throws Exception {
        String registerBody = """
                {
                  "firstName": "Mostafa",
                  "lastName": "Mahmoud",
                  "email": "Mostafa@Example.com",
                  "password": "strong-password-123"
                }
                """;

        var registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("mostafa@example.com"))
                .andReturn();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"mostafa@example.com","password":"strong-password-123"}
                                """))
                .andExpect(status().isForbidden());

        ArgumentCaptor<String> activationToken = ArgumentCaptor.forClass(String.class);
        verify(mailService).sendActivationEmail(any(), activationToken.capture());
        mockMvc.perform(get("/api/v1/auth/activate").param("token", activationToken.getValue()))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("الخبراء المثمنين للخبرة والتثمين")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("/brand/el-habashy-logo.png")));

        var loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"mostafa@example.com","password":"strong-password-123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(cookie().httpOnly("elhabashy_refresh", true))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andExpect(jsonPath("$.expiresAt").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("mostafa@example.com"))
                .andReturn();

        String accessToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.accessToken");
        Cookie refreshCookie = loginResult.getResponse().getCookie("elhabashy_refresh");

        mockMvc.perform(get("/api/v1/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("mostafa@example.com"));

        mockMvc.perform(get("/api/v1/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));

        mockMvc.perform(post("/api/v1/auth/refresh").cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().httpOnly("elhabashy_refresh", true))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresAt").isNotEmpty());

        var user = userRepository.findByEmailIgnoreCase("mostafa@example.com").orElseThrow();
        user.setRole(Role.ADMIN);
        userRepository.saveAndFlush(user);

        var adminLoginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"mostafa@example.com","password":"strong-password-123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("ADMIN"))
                .andReturn();
        String adminToken = JsonPath.read(adminLoginResult.getResponse().getContentAsString(), "$.accessToken");

        mockMvc.perform(get("/api/v1/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("mostafa@example.com"));
    }

    @Test
    void invalidLoginAndDuplicateRegistrationReturnSafeErrors() throws Exception {
        String body = """
                {
                  "firstName": "Test",
                  "lastName": "User",
                  "email": "test@example.com",
                  "password": "strong-password-123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        ArgumentCaptor<String> activationToken = ArgumentCaptor.forClass(String.class);
        verify(mailService).sendActivationEmail(any(), activationToken.capture());
        mockMvc.perform(get("/api/v1/auth/activate").param("token", activationToken.getValue()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"test@example.com","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid authentication credentials"));
    }

    @Test
    void corsPreflightAllowsLocalReactOnAnyPortAndCustomHeaders() throws Exception {
        String origin = "http://localhost:3000";

        mockMvc.perform(options("/api/v1/auth/login")
                        .header(HttpHeaders.ORIGIN, origin)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type,x-requested-with"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    void forgotPasswordOtpResetsPasswordAndRejectsOldPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName":"Reset",
                                  "lastName":"User",
                                  "email":"reset@example.com",
                                  "password":"old-password-123"
                                }
                                """))
                .andExpect(status().isCreated());

        ArgumentCaptor<String> activationToken = ArgumentCaptor.forClass(String.class);
        verify(mailService).sendActivationEmail(any(), activationToken.capture());
        mockMvc.perform(get("/api/v1/auth/activate").param("token", activationToken.getValue()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"reset@example.com\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<String> otp = ArgumentCaptor.forClass(String.class);
        verify(mailService).sendPasswordResetOtp(any(), otp.capture());

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"reset@example.com","otp":"000000","newPassword":"new-password-456"}
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"reset@example.com","otp":"%s","newPassword":"new-password-456"}
                                """.formatted(otp.getValue())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"reset@example.com\",\"password\":\"old-password-123\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"reset@example.com\",\"password\":\"new-password-456\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void publicEndpointsIgnoreInvalidBearerAndProtectedEndpointsReturnProblemJson() throws Exception {
        mockMvc.perform(get("/brand/el-habashy-logo.png"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .contentType(MediaType.IMAGE_PNG));

        mockMvc.perform(post("/api/v1/auth/register")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer null")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation failed"));

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.detail")
                        .value("Authentication is required or the access token is invalid."));
    }

    @Test
    void smtpFailureReturnsServiceUnavailableInsteadOfUnauthorizedErrorDispatch() throws Exception {
        doThrow(new MailSendException("SMTP unavailable"))
                .when(mailService).sendActivationEmail(any(), any());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName":"Mail",
                                  "lastName":"Failure",
                                  "email":"mail-failure@example.com",
                                  "password":"strong-password-123"
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.errorCode").value("EMAIL_SERVICE_UNAVAILABLE"));
    }
}
