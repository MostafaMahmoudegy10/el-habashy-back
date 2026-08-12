package com.example.elhabashyback.listing;

import com.example.elhabashyback.auth.service.JwtTokenService;
import com.example.elhabashyback.listing.entity.MediaRole;
import com.example.elhabashyback.listing.service.ListingMediaStateService;
import com.example.elhabashyback.listing.service.ListingMediaUploadWorker;
import com.example.elhabashyback.listing.service.PendingListingMedia;
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
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
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
    private ListingMediaUploadWorker mediaUploadWorker;

    @Autowired
    private ListingMediaStateService mediaStateService;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void createAccessTokens() {
        Users admin = userRepository.findByEmailIgnoreCase("mostafa.mahmoudegy10@gmail.com").orElseThrow();
        adminToken = jwtTokenService.createAccessToken(admin).value();

        Users user = userRepository.findByEmailIgnoreCase("listing-user@example.com")
                .orElseGet(this::createUser);
        userToken = jwtTokenService.createAccessToken(user).value();
        when(stagingStorage.stage(any(), anyLong())).thenAnswer(invocation ->
                Path.of("build", "staged-" + invocation.<Long>getArgument(1)));
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

        mockMvc.perform(get("/api/v1/public/listings")
                        .param("q", "private garden"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].slug").value("new-cairo-private-villa"));
    }

    @Test
    void publicCanReadAVisibleListingBySlugButNotAnInactiveOne() throws Exception {
        String firstResponse = mockMvc.perform(get("/api/v1/public/listings/new-cairo-private-villa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("new-cairo-private-villa"))
                .andExpect(jsonPath("$.title.ar").isNotEmpty())
                .andExpect(jsonPath("$.title.en").isNotEmpty())
                .andExpect(jsonPath("$.images.length()").value(2))
                .andExpect(jsonPath("$.specs.length()").value(3))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number firstViews = JsonPath.read(firstResponse, "$.views");
        Number firstClicks = JsonPath.read(firstResponse, "$.whatsappClicks");

        mockMvc.perform(get("/api/v1/public/listings/new-cairo-private-villa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.views").value(firstViews.longValue() + 1));

        mockMvc.perform(post("/api/v1/public/listings/new-cairo-private-villa/whatsapp-click"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("new-cairo-private-villa"))
                .andExpect(jsonPath("$.whatsappClicks").value(firstClicks.longValue() + 1));

        mockMvc.perform(get("/api/v1/public/listings/transport-vehicles"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/public/listings/transport-vehicles/whatsapp-click"))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCanCreateUpdateChangeStatusAndDeleteAListing() throws Exception {
        String searchableBody = CREATE_BODY.replace("\"en\": \"Count\"", "\"en\": \"forensiczeta\"");
        String createdJson = mockMvc.perform(multipart("/api/v1/admin/listings")
                        .file(listingPart(searchableBody))
                        .file(image("thumbnail", "thumbnail.png"))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                )
                .andExpect(status().isAccepted())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/v1/public/listings/postman-test-equipment-auction"))
                .andExpect(jsonPath("$.title.en").value("Test Equipment Auction"))
                .andExpect(jsonPath("$.featured").value(true))
                .andExpect(jsonPath("$.media.length()").value(1))
                .andExpect(jsonPath("$.media[0].role").value("thumbnail"))
                .andExpect(jsonPath("$.media[0].status").value("uploading"))
                .andExpect(jsonPath("$.views").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(mediaUploadWorker).upload(any());

        Number id = JsonPath.read(createdJson, "$.id");

        mockMvc.perform(get("/api/v1/public/listings")
                        .param("q", "forensiczeta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].slug").value("postman-test-equipment-auction"));

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
        mockMvc.perform(multipart("/api/v1/admin/listings")
                        .file(listingPart(CREATE_BODY))
                        .file(image("thumbnail", "thumbnail.png")))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(multipart("/api/v1/admin/listings")
                        .file(listingPart(CREATE_BODY))
                        .file(image("thumbnail", "thumbnail.png"))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void createValidatesLocalizedFieldsAndDateRange() throws Exception {
        mockMvc.perform(multipart("/api/v1/admin/listings")
                        .file(listingPart(CREATE_BODY.replace("\"en\": \"Test Equipment Auction\"", "\"en\": \"\"")))
                        .file(image("thumbnail", "thumbnail.png"))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['title.en']").exists());

        mockMvc.perform(multipart("/api/v1/admin/listings")
                        .file(listingPart(CREATE_BODY.replace("2026-09-01", "2026-07-01")))
                        .file(image("thumbnail", "thumbnail.png"))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("expireDate cannot be before publishDate"));

        mockMvc.perform(multipart("/api/v1/admin/listings")
                        .file(listingPart(CREATE_BODY))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Required multipart part is missing: thumbnail"));

        String invalidMediaBody = CREATE_BODY.replace(
                "postman-test-equipment-auction",
                "invalid-media-must-rollback");
        mockMvc.perform(multipart("/api/v1/admin/listings")
                        .file(listingPart(invalidMediaBody))
                        .file(new MockMultipartFile(
                                "thumbnail", "thumbnail.txt", "text/plain", new byte[]{1, 2, 3}))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Unsupported image content type"));

        mockMvc.perform(get("/api/v1/public/listings/invalid-media-must-rollback"))
                .andExpect(status().isNotFound());
    }

    @Test
    void wholeListingSubmissionQueuesThumbnailGalleryAndVideoTogether() throws Exception {
        String body = CREATE_BODY.replace(
                "postman-test-equipment-auction",
                "complete-listing-submission-test")
                .replace("\"category\": \"movables\"", "\"category\": \"real-estate\"")
                .replace("\"en\": \"Count\"", "\"en\": \"Area\"")
                .replace("\"en\": \"10 items\"", "\"en\": \"240 sqm\"");

        String createdJson = mockMvc.perform(multipart("/api/v1/admin/listings")
                        .file(listingPart(body))
                        .file(image("thumbnail", "main.png"))
                        .file(image("gallery", "gallery-1.png"))
                        .file(image("gallery", "gallery-2.png"))
                        .file(new MockMultipartFile(
                                "video", "auction.mp4", "video/mp4", new byte[]{1, 2, 3, 4, 5}))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.slug").value("complete-listing-submission-test"))
                .andExpect(jsonPath("$.category").value("real-estate"))
                .andExpect(jsonPath("$.featured").value(true))
                .andExpect(jsonPath("$.specs.length()").value(1))
                .andExpect(jsonPath("$.specs[0].label.en").value("Area"))
                .andExpect(jsonPath("$.specs[0].value.en").value("240 sqm"))
                .andExpect(jsonPath("$.media.length()").value(4))
                .andExpect(jsonPath("$.media[0].role").value("thumbnail"))
                .andExpect(jsonPath("$.media[1].role").value("gallery"))
                .andExpect(jsonPath("$.media[2].role").value("gallery"))
                .andExpect(jsonPath("$.media[3].role").value("video"))
                .andExpect(jsonPath("$.media[0].status").value("uploading"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(mediaUploadWorker, times(4)).upload(any());
        Number listingId = JsonPath.read(createdJson, "$.id");
        mockMvc.perform(delete("/api/v1/admin/listings/{id}", listingId.longValue())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void verifiedCloudinaryResultPersistsPublicIdAndUrl() throws Exception {
        String listingJson = mockMvc.perform(get("/api/v1/public/listings/new-cairo-private-villa"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Number listingId = JsonPath.read(listingJson, "$.id");

        PendingListingMedia pending = mediaStateService.createPending(
                listingId.longValue(),
                com.example.elhabashyback.listing.entity.MediaType.IMAGE,
                MediaRole.GALLERY,
                "verified-gallery.png",
                "image/png",
                4
        );
        String secureUrl = "https://res.cloudinary.com/test-cloud/image/upload/verified-gallery.png";
        mediaStateService.markReady(
                listingId.longValue(),
                pending.mediaId(),
                new CloudinaryUploadResult(
                        secureUrl,
                        pending.publicId(),
                        "image",
                        "png",
                        800,
                        600,
                        4,
                        null,
                        1719307544L,
                        "verified-by-client"
                )
        );

        mockMvc.perform(get("/api/v1/admin/listings/{listingId}/media/{mediaId}",
                        listingId.longValue(), pending.mediaId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ready"))
                .andExpect(jsonPath("$.publicId").value(pending.publicId()))
                .andExpect(jsonPath("$.url").value(secureUrl));
    }

    private MockMultipartFile listingPart(String body) {
        return new MockMultipartFile(
                "listing",
                "listing.json",
                MediaType.APPLICATION_JSON_VALUE,
                body.getBytes(StandardCharsets.UTF_8)
        );
    }

    private MockMultipartFile image(String partName, String fileName) {
        return new MockMultipartFile(
                partName,
                fileName,
                "image/png",
                new byte[]{1, 2, 3, 4}
        );
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
