package com.example.elhabashyback.listing.importer.dto;

import java.util.List;
import java.util.Map;

public record WorkbookPreviewResponse(
        List<SheetResponse> sheets,
        int selectedSheetIndex,
        int headerRow,
        List<ColumnResponse> columns,
        List<RowResponse> rows,
        int totalRows
) {
    public record SheetResponse(
            int index,
            String name,
            int physicalRows
    ) {
    }

    public record ColumnResponse(
            String key,
            String header,
            int index
    ) {
    }

    public record RowResponse(
            int rowNumber,
            Map<String, String> values,
            List<EmbeddedImageResponse> images
    ) {
    }

    public record EmbeddedImageResponse(
            String columnKey,
            String fileName,
            String contentType,
            String dataBase64
    ) {
    }
}
