package com.example.elhabashyback.expertise;

import com.example.elhabashyback.auth.service.JwtTokenService;
import com.example.elhabashyback.user.entity.Users;
import com.example.elhabashyback.user.repoistory.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ServiceArticleControllerIntegrationTests {

    private static final String CREATE_BODY = """
            {
              "kind": "consulting",
              "title": {"ar": "استشارات اختبارية", "en": "Integration Test Consulting"},
              "summary": {"ar": "ملخص عربي", "en": "English summary"},
              "content": {"ar": "<h2>المحتوى</h2><p>تفاصيل عربية</p>", "en": "<h2>Content</h2><p>English details</p>"},
              "image": "https://example.com/hero.jpg",
              "gallery": ["https://example.com/gallery.jpg"],
              "featured": true,
              "displayOrder": 20,
              "seoTitle": {"ar": "عنوان البحث", "en": "Search title"},
              "seoDescription": {"ar": "وصف البحث", "en": "Search description"},
              "seoKeywords": {"ar": "استشارات", "en": "consulting"}
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
    void publicReadsSeededArticlesAndFiltersByKind() throws Exception {
        mockMvc.perform(get("/api/v1/public/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[0].image").isNotEmpty())
                .andExpect(jsonPath("$[0].gallery").isArray());

        mockMvc.perform(get("/api/v1/public/services").param("kind", "valuation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].kind").value("valuation"));

        mockMvc.perform(get("/api/v1/public/services/real-estate-arbitration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title.en").value("Real Estate Arbitration"));

        mockMvc.perform(get("/api/v1/public/services").param("kind", "unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Unsupported service kind: unknown"));
    }

    @Test
    void adminCreatesUpdatesAndDeletesAnArticle() throws Exception {
        String createdJson = mockMvc.perform(post("/api/v1/admin/services")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("integration-test-consulting"))
                .andExpect(jsonPath("$.seoTitle.en").value("Search title"))
                .andReturn().getResponse().getContentAsString();

        Number id = JsonPath.read(createdJson, "$.id");
        mockMvc.perform(put("/api/v1/admin/services/{id}", id.longValue())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_BODY.replace("English summary", "Updated summary")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.en").value("Updated summary"));

        mockMvc.perform(delete("/api/v1/admin/services/{id}", id.longValue())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/public/services/integration-test-consulting"))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/admin/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_BODY))
                .andExpect(status().isUnauthorized());
    }
}
