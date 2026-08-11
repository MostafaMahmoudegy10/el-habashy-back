package com.example.elhabashyback.about;

import com.example.elhabashyback.about.dto.AboutImageResponse;
import com.example.elhabashyback.about.service.AboutImageService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AboutControllerIntegrationTests {

    private static final String PERSON_BODY = """
            {
              "name": {"ar": "خبير اختبار", "en": "Test Expert"},
              "role": {"ar": "خبير مثمن", "en": "Valuation Expert"},
              "biography": {"ar": "نبذة مهنية للاختبار", "en": "Professional test biography"},
              "imageUrl": "https://cdn.example.com/expert.webp",
              "displayOrder": 99,
              "active": false
            }
            """;

    private static final String ACTIVE_PERSON_BODY = PERSON_BODY.replace("\"active\": false", "\"active\": true");

    private static final String CATEGORY_BODY = """
            {
              "title": {"ar": "مشروعات الاختبار", "en": "Test Projects"},
              "summary": {"ar": "ملخص التصنيف", "en": "Category summary"},
              "displayOrder": 98
            }
            """;

    private static final String ENTRY_BODY = """
            {
              "title": {"ar": "تقييم أسطول سيارات", "en": "Vehicle Fleet Valuation"},
              "client": {"ar": "عميل الاختبار", "en": "Test Client"},
              "summary": {"ar": "فحص وتقييم أسطول", "en": "Fleet inspection and valuation"},
              "details": {"ar": "تمت معاينة المركبات وتوثيق حالتها", "en": "Vehicles were inspected and documented"},
              "projectYear": 2026,
              "location": {"ar": "القاهرة", "en": "Cairo"},
              "imageUrl": "https://cdn.example.com/fleet.webp",
              "displayOrder": 0
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private AboutImageService imageService;

    private String adminToken;

    @BeforeEach
    void createAdminToken() {
        Users admin = userRepository.findByEmailIgnoreCase("mostafa.mahmoudegy10@gmail.com").orElseThrow();
        adminToken = jwtTokenService.createAccessToken(admin).value();
    }

    @Test
    void publicAboutReturnsStructuredBilingualContentAndAdminRoutesAreProtected() throws Exception {
        mockMvc.perform(get("/api/v1/public/about"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.headline.ar").isNotEmpty())
                .andExpect(jsonPath("$.profile.mission.en").isNotEmpty())
                .andExpect(jsonPath("$.people").isArray())
                .andExpect(jsonPath("$.departments").isArray())
                .andExpect(jsonPath("$.certificates").isArray())
                .andExpect(jsonPath("$.workCategories").isArray());

        mockMvc.perform(post("/api/v1/admin/about/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PERSON_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanCreateHideActivateUpdateAndDeleteOrganizationPeople() throws Exception {
        String response = mockMvc.perform(post("/api/v1/admin/about/people")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PERSON_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.imageUrl").value("https://cdn.example.com/expert.webp"))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(response, "$.id");

        mockMvc.perform(get("/api/v1/admin/about")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.people[*].name.en", hasItem("Test Expert")));

        mockMvc.perform(get("/api/v1/public/about"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.people[*].name.en", not(hasItem("Test Expert"))));

        mockMvc.perform(put("/api/v1/admin/about/people/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ACTIVE_PERSON_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(get("/api/v1/public/about"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.people[*].name.en", hasItem("Test Expert")));

        mockMvc.perform(delete("/api/v1/admin/about/people/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void adminCanCreateDetailedPreviousWorkAndUploadImages() throws Exception {
        String categoryResponse = mockMvc.perform(post("/api/v1/admin/about/work-categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CATEGORY_BODY))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer categoryId = JsonPath.read(categoryResponse, "$.id");

        mockMvc.perform(post("/api/v1/admin/about/work-categories/{id}/entries", categoryId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ENTRY_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.client.en").value("Test Client"))
                .andExpect(jsonPath("$.projectYear").value(2026))
                .andExpect(jsonPath("$.details.ar").isNotEmpty());

        mockMvc.perform(get("/api/v1/public/about"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workCategories[*].title.en", hasItem("Test Projects")))
                .andExpect(jsonPath("$.workCategories[?(@.title.en == 'Test Projects')].entries[0].client.en")
                        .value("Test Client"));

        when(imageService.upload(any())).thenReturn(new AboutImageResponse(
                "https://cdn.example.com/about/image.webp",
                "el-habashy/about/image",
                "webp",
                1200,
                800,
                1024L
        ));
        MockMultipartFile image = new MockMultipartFile(
                "file", "person.webp", "image/webp", new byte[]{1, 2, 3}
        );
        mockMvc.perform(multipart("/api/v1/admin/about/media")
                        .file(image)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://cdn.example.com/about/image.webp"));

        mockMvc.perform(delete("/api/v1/admin/about/work-categories/{id}", categoryId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}
