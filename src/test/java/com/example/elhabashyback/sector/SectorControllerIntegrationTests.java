package com.example.elhabashyback.sector;

import com.example.elhabashyback.auth.service.JwtTokenService;
import com.example.elhabashyback.user.entity.Role;
import com.example.elhabashyback.user.entity.Users;
import com.example.elhabashyback.user.repoistory.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SectorControllerIntegrationTests {

    private static final String UPDATE_BODY = """
            {
              "title": {
                "ar": "العقارات المحدثة",
                "en": "Updated Real Estate"
              },
              "description": {
                "ar": "وصف عربي محدث للقطاع.",
                "en": "Updated English sector description."
              }
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void createAccessTokens() {
        Users admin = userRepository.findByEmailIgnoreCase("mostafa.mahmoudegy10@gmail.com").orElseThrow();
        adminToken = jwtTokenService.createAccessToken(admin).value();

        Users user = userRepository.findByEmailIgnoreCase("sector-user@example.com")
                .orElseGet(() -> createUser("sector-user@example.com"));
        userToken = jwtTokenService.createAccessToken(user).value();
    }

    @Test
    void publicListReturnsTheSixSeededSectorsInDisplayOrder() throws Exception {
        mockMvc.perform(get("/api/v1/public/sectors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[0].code").value("real-estate"))
                .andExpect(jsonPath("$[0].displayOrder").value(0))
                .andExpect(jsonPath("$[0].title.ar").isNotEmpty())
                .andExpect(jsonPath("$[0].description.en").isNotEmpty())
                .andExpect(jsonPath("$[5].code").value("other"));
    }

    @Test
    void adminCanUpdateASectorAndThePublicApiReturnsTheChange() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/sectors/real-estate")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("real-estate"))
                .andExpect(jsonPath("$.title.ar").value("العقارات المحدثة"))
                .andExpect(jsonPath("$.title.en").value("Updated Real Estate"))
                .andExpect(jsonPath("$.description.en").value("Updated English sector description."));

        mockMvc.perform(get("/api/v1/public/sectors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title.en").value("Updated Real Estate"));
    }

    @Test
    void updateRequiresAdminRole() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/sectors/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHENTICATED"));

        mockMvc.perform(patch("/api/v1/admin/sectors/cars")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
    }

    @Test
    void updateValidatesAllBilingualFields() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/sectors/cars")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": {"ar": "", "en": "Cars"},
                                  "description": {"ar": "وصف", "en": ""}
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.errors['title.ar']").exists())
                .andExpect(jsonPath("$.errors['description.en']").exists());
    }

    @Test
    void updateReturnsNotFoundForUnknownSectorCode() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/sectors/unknown")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Sector not found"));
    }

    private Users createUser(String email) {
        Users user = new Users();
        user.setFirstName("Sector");
        user.setLastName("User");
        user.setEmail(email);
        user.setPassword("not-used-for-this-test");
        user.setEnabled(true);
        user.setRole(Role.USER);
        return userRepository.saveAndFlush(user);
    }
}
