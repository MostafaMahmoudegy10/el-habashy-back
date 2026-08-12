package com.example.elhabashyback.listing.importer.controller;

import com.example.elhabashyback.listing.importer.dto.WorkbookPreviewResponse;
import com.example.elhabashyback.listing.importer.service.ExcelListingImportService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/listing-imports")
@RequiredArgsConstructor
@Validated
public class AdminListingImportController {

    private final ExcelListingImportService importService;

    @PostMapping(value = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    WorkbookPreviewResponse preview(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "0") @Min(0) int sheetIndex,
            @RequestParam(defaultValue = "1") @Min(1) int headerRow
    ) {
        return importService.preview(file, sheetIndex, headerRow);
    }
}
