package com.example.elhabashyback.settings;

import com.example.elhabashyback.auth.service.JwtTokenService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AppSettingsControllerIntegrationTests {

    private static final String UPDATE_BODY = """
            {
              "whatsappNumber": "+201111111111",
              "whatsappMessageAr": "أحتاج تفاصيل {title} في قسم {category}",
              "whatsappMessageEn": "I need details for {title} in {category}",
              "contactPhone": "+2025780424",
              "contactEmail": "contact@elhabashy.test",
              "officeAddress": {
                "ar": "القاهرة - قصر النيل",
                "en": "Kasr El Nil, Cairo"
              },
              "mapUrl": "https://maps.google.com/?q=Kasr+El+Nil",
              "facebookUrl": "https://facebook.com/elhabashy",
              "linkedinUrl": "https://linkedin.com/company/elhabashy"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    private String adminToken;

    @BeforeEach
    void createAdminToken() {
        Users admin = userRepository.findByEmailIgnoreCase("mostafa.mahmoudegy10@gmail.com").orElseThrow();
        adminToken = jwtTokenService.createAccessToken(admin).value();
    }

    @Test
    void publicCanReadSettingsAndOnlyAdminCanUpdateThem() throws Exception {
        mockMvc.perform(get("/api/v1/public/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.whatsappNumber").isNotEmpty())
                .andExpect(jsonPath("$.officeAddress.ar").isNotEmpty());

        mockMvc.perform(put("/api/v1/admin/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_BODY))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/v1/admin/settings")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.whatsappNumber").value("+201111111111"))
                .andExpect(jsonPath("$.contactEmail").value("contact@elhabashy.test"))
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());

        mockMvc.perform(get("/api/v1/public/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.whatsappNumber").value("+201111111111"))
                .andExpect(jsonPath("$.officeAddress.en").value("Kasr El Nil, Cairo"));
    }
}
