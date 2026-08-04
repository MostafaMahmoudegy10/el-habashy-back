package com.example.elhabashyback.listing;

import com.example.elhabashyback.auth.service.JwtTokenService;
import com.example.elhabashyback.listing.service.ListingVideoUploadWorker;
import com.example.elhabashyback.media.service.CloudinaryUploadClient;
import com.example.elhabashyback.media.service.CloudinaryUploadResult;
import com.example.elhabashyback.media.service.MediaStagingStorage;
import com.example.elhabashyback.user.entity.Role;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ListingControllerIntegrationTests {

    private static final String CREATE_BODY = """
            {
              "slug": "postman-test-equipment-auction",
              "title": {"ar": "مزاد معدات للاختبار", "en": "Test Equipment Auction"},
              "summary": {"ar": "بيانات ملخصة للاختبار", "en": "Test listing summary"},
              "description": {"ar": "<p>وصف عربي كامل</p>", "en": "<p>Full English description</p>"},
              "category": "movables",
              "status": "active",
              "city": {"ar": "القاهرة", "en": "Cairo"},
              "location": {"ar": "مدينة نصر", "en": "Nasr City"},
              "priceLabel": {"ar": "حسب المعاينة", "en": "Based on inspection"},
              "measureLabel": "10 items",
              "featured": true,
              "specs": [
                {
                  "label": {"ar": "العدد", "en": "Count"},
                  "value": {"ar": "10 قطع", "en": "10 items"}
                }
              ],
              "publishDate": "2026-08-01",
              "expireDate": "2026-09-01",
              "auctionDate": "2026-08-25",
              "auctionTime": "12:30",
              "beneficiary": {"ar": "شركة اختبار", "en": "Test Company"},
              "venue": {"ar": "قاعة المزاد", "en": "Auction Hall"},
              "announcementSource": {"ar": "إعلان رسمي", "en": "Official announcement"},
              "notes": {"ar": "المعاينة بالحجز", "en": "Inspection by appointment"},
              "mapUrl": "https://maps.google.com/?q=Cairo",
              "whatsappPhone": "+201000000000",
              "seoTitle": {"ar": "مزاد معدات", "en": "Equipment Auction"},
              "seoDescription": {"ar": "وصف محركات البحث", "en": "SEO description"},
              "seoKeywords": {"ar": "مزاد، معدات", "en": "auction, equipment"}
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private CloudinaryUploadClient cloudinaryUploadClient;

    @MockitoBean
    private MediaStagingStorage stagingStorage;

    @MockitoBean
    private ListingVideoUploadWorker videoUploadWorker;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void createAccessTokens() {
        Users admin = userRepository.findByEmailIgnoreCase("mostafa.mahmoudegy10@gmail.com").orElseThrow();
        adminToken = jwtTokenService.createAccessToken(admin).value();

        Users user = userRepository.findByEmailIgnoreCase("listing-user@example.com")
                .orElseGet(this::createUser);
        userToken = jwtTokenService.createAccessToken(user).value();
    }

    @Test
    void publicListSupportsPaginationFiltersAndHidesInactiveListings() throws Exception {
        mockMvc.perform(get("/api/v1/public/listings")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5));

        mockMvc.perform(get("/api/v1/public/listings")
                        .param("category", "real-estate")
                        .param("featured", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].category").value("real-estate"));
    }

    @Test
    void publicCanReadAVisibleListingBySlugButNotAnInactiveOne() throws Exception {
        mockMvc.perform(get("/api/v1/public/listings/new-cairo-private-villa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("new-cairo-private-villa"))
                .andExpect(jsonPath("$.title.ar").isNotEmpty())
                .andExpect(jsonPath("$.title.en").isNotEmpty())
                .andExpect(jsonPath("$.images.length()").value(2))
                .andExpect(jsonPath("$.specs.length()").value(3));

        mockMvc.perform(get("/api/v1/public/listings/transport-vehicles"))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCanCreateUpdateChangeStatusAndDeleteAListing() throws Exception {
        String createdJson = mockMvc.perform(post("/api/v1/admin/listings")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/v1/public/listings/postman-test-equipment-auction"))
                .andExpect(jsonPath("$.title.en").value("Test Equipment Auction"))
                .andExpect(jsonPath("$.views").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number id = JsonPath.read(createdJson, "$.id");
        String updatedBody = CREATE_BODY
                .replace("Test Equipment Auction", "Updated Equipment Auction")
                .replace("مزاد معدات للاختبار", "مزاد معدات محدث للاختبار");

        mockMvc.perform(put("/api/v1/admin/listings/{id}", id.longValue())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title.en").value("Updated Equipment Auction"));

        mockMvc.perform(patch("/api/v1/admin/listings/{id}/status", id.longValue())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"closed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("closed"));

        mockMvc.perform(delete("/api/v1/admin/listings/{id}", id.longValue())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/public/listings/postman-test-equipment-auction"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listingWritesRequireAnAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/admin/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_BODY))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/admin/listings")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void createValidatesLocalizedFieldsAndDateRange() throws Exception {
        mockMvc.perform(post("/api/v1/admin/listings")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_BODY.replace("\"en\": \"Test Equipment Auction\"", "\"en\": \"\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['title.en']").exists());

        mockMvc.perform(post("/api/v1/admin/listings")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_BODY.replace("2026-09-01", "2026-07-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("expireDate cannot be before publishDate"));
    }

    @Test
    void backendUploadsMultipartImagesAndQueuesChunkedVideosForAdminsOnly() throws Exception {
        String listingJson = mockMvc.perform(get("/api/v1/public/listings/new-cairo-private-villa"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Number listingId = JsonPath.read(listingJson, "$.id");

        MockMultipartFile image = new MockMultipartFile(
                "file", "gallery.png", "image/png", new byte[]{1, 2, 3, 4});
        when(cloudinaryUploadClient.uploadImage(any(), anyString(), eq("image/png")))
                .thenAnswer(invocation -> new CloudinaryUploadResult(
                        "https://res.cloudinary.com/test-cloud/image/upload/gallery.png",
                        invocation.getArgument(1),
                        "image",
                        "png",
                        800,
                        600,
                        4,
                        null,
                        1719307544L,
                        "verified-by-client"
                ));

        String uploadedImageJson = mockMvc.perform(multipart(
                                "/api/v1/admin/listings/{id}/media/images/gallery", listingId.longValue())
                        .file(image)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("image"))
                .andExpect(jsonPath("$.role").value("gallery"))
                .andExpect(jsonPath("$.status").value("ready"))
                .andExpect(jsonPath("$.url").value(
                        "https://res.cloudinary.com/test-cloud/image/upload/gallery.png"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Number imageMediaId = JsonPath.read(uploadedImageJson, "$.id");

        mockMvc.perform(get("/api/v1/public/listings/new-cairo-private-villa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.images.length()").value(3))
                .andExpect(jsonPath("$.media.length()").value(3));

        MockMultipartFile video = new MockMultipartFile(
                "file", "auction-video.mp4", "video/mp4", new byte[]{1, 2, 3, 4, 5});
        Path stagedVideo = Path.of("build", "test-staged-video.mp4");
        when(stagingStorage.stage(any(), anyLong())).thenReturn(stagedVideo);

        String queuedVideoJson = mockMvc.perform(multipart(
                                "/api/v1/admin/listings/{id}/media/videos", listingId.longValue())
                        .file(video)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.type").value("video"))
                .andExpect(jsonPath("$.role").value("video"))
                .andExpect(jsonPath("$.status").value("uploading"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(videoUploadWorker).upload(any());
        Number videoMediaId = JsonPath.read(queuedVideoJson, "$.id");

        mockMvc.perform(get("/api/v1/admin/listings/{listingId}/media/{mediaId}",
                        listingId.longValue(), videoMediaId.longValue())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("uploading"));

        mockMvc.perform(multipart("/api/v1/admin/listings/{id}/media/videos", listingId.longValue())
                        .file(video)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isConflict());

        mockMvc.perform(multipart("/api/v1/admin/listings/{id}/media/images/gallery", listingId.longValue())
                        .file(image)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/admin/listings/{listingId}/media/{mediaId}",
                        listingId.longValue(), imageMediaId.longValue())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    private Users createUser() {
        Users user = new Users();
        user.setFirstName("Listing");
        user.setLastName("User");
        user.setEmail("listing-user@example.com");
        user.setPassword("not-used-for-this-test");
        user.setEnabled(true);
        user.setRole(Role.USER);
        return userRepository.saveAndFlush(user);
    }

}
