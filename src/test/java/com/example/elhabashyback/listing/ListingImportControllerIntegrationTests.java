package com.example.elhabashyback.listing;

import com.example.elhabashyback.auth.service.JwtTokenService;
import com.example.elhabashyback.user.entity.Users;
import com.example.elhabashyback.user.repoistory.UserRepository;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ListingImportControllerIntegrationTests {

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
    void adminCanSelectASheetAndPreviewMappedRows() throws Exception {
        mockMvc.perform(multipart("/api/v1/admin/listing-imports/preview")
                        .file(workbook())
                        .param("sheetIndex", "1")
                        .param("headerRow", "1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sheets.length()").value(2))
                .andExpect(jsonPath("$.selectedSheetIndex").value(1))
                .andExpect(jsonPath("$.columns[0].key").value("A"))
                .andExpect(jsonPath("$.columns[0].header").value("العنوان العربي"))
                .andExpect(jsonPath("$.rows[0].rowNumber").value(2))
                .andExpect(jsonPath("$.rows[0].values.A").value("مزاد تحف"))
                .andExpect(jsonPath("$.rows[0].values.B").value("Antiques auction"))
                .andExpect(jsonPath("$.rows[0].images.length()").value(1))
                .andExpect(jsonPath("$.rows[0].images[0].columnKey").value("D"))
                .andExpect(jsonPath("$.rows[0].images[0].contentType").value("image/png"))
                .andExpect(jsonPath("$.totalRows").value(1));
    }

    @Test
    void previewRejectsNonExcelFiles() throws Exception {
        MockMultipartFile textFile = new MockMultipartFile(
                "file", "listings.csv", "text/csv", "title".getBytes());
        mockMvc.perform(multipart("/api/v1/admin/listing-imports/preview")
                        .file(textFile)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Only .xlsx and .xls files are supported"));
    }

    private MockMultipartFile workbook() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            workbook.createSheet("تعليمات").createRow(0).createCell(0).setCellValue("اترك هذه الورقة");
            var listings = workbook.createSheet("الإعلانات");
            var header = listings.createRow(0);
            header.createCell(0).setCellValue("العنوان العربي");
            header.createCell(1).setCellValue("English title");
            header.createCell(2).setCellValue("القسم");
            header.createCell(3).setCellValue("الصورة الرئيسية");
            var row = listings.createRow(1);
            row.createCell(0).setCellValue("مزاد تحف");
            row.createCell(1).setCellValue("Antiques auction");
            row.createCell(2).setCellValue("antiques");
            int pictureIndex = workbook.addPicture(
                    Base64.getDecoder().decode(
                            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII="),
                    XSSFWorkbook.PICTURE_TYPE_PNG
            );
            var drawing = listings.createDrawingPatriarch();
            var anchor = workbook.getCreationHelper().createClientAnchor();
            anchor.setRow1(1);
            anchor.setCol1(3);
            drawing.createPicture(anchor, pictureIndex);
            workbook.write(output);
            return new MockMultipartFile(
                    "file",
                    "listings.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    output.toByteArray()
            );
        }
    }
}
