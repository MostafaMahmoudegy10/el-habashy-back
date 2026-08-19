package com.example.elhabashyback.listing.importer.service;

import com.example.elhabashyback.common.exception.BadRequestException;
import com.example.elhabashyback.listing.importer.dto.WorkbookPreviewResponse;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Shape;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ExcelListingImportService {

    private static final long MAX_WORKBOOK_BYTES = 15L * 1024L * 1024L;
    private static final int MAX_DATA_ROWS = 500;
    private static final int MAX_COLUMNS = 80;
    private static final int MAX_CELL_CHARACTERS = 20_000;
    private static final int MAX_EMBEDDED_IMAGES = 10_500;

    public WorkbookPreviewResponse preview(MultipartFile file, int sheetIndex, int headerRow) {
        validateFile(file);
        if (headerRow < 1) {
            throw new BadRequestException("headerRow must be 1 or greater");
        }

        DataFormatter formatter = new DataFormatter(Locale.ROOT, true);
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new BadRequestException("The workbook does not contain any sheets");
            }
            if (sheetIndex < 0 || sheetIndex >= workbook.getNumberOfSheets()) {
                throw new BadRequestException("Unknown workbook sheet index");
            }

            List<WorkbookPreviewResponse.SheetResponse> sheets = new ArrayList<>();
            for (int index = 0; index < workbook.getNumberOfSheets(); index++) {
                Sheet sheet = workbook.getSheetAt(index);
                sheets.add(new WorkbookPreviewResponse.SheetResponse(
                        index,
                        sheet.getSheetName(),
                        sheet.getPhysicalNumberOfRows()
                ));
            }

            Sheet selected = workbook.getSheetAt(sheetIndex);
            int headerIndex = headerRow - 1;
            Row header = selected.getRow(headerIndex);
            if (header == null || isBlank(header, formatter)) {
                throw new BadRequestException("The selected header row is empty");
            }

            int columnCount = findColumnCount(selected, headerIndex);
            if (columnCount == 0) {
                throw new BadRequestException("The selected sheet does not contain columns");
            }
            if (columnCount > MAX_COLUMNS) {
                throw new BadRequestException("Excel imports support up to 80 columns");
            }

            List<WorkbookPreviewResponse.ColumnResponse> columns = new ArrayList<>();
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                String key = CellReference.convertNumToColString(columnIndex);
                String label = value(header.getCell(columnIndex), formatter);
                columns.add(new WorkbookPreviewResponse.ColumnResponse(
                        key,
                        label.isBlank() ? "Column " + key : label,
                        columnIndex
                ));
            }

            Map<Integer, List<WorkbookPreviewResponse.EmbeddedImageResponse>> imagesByRow =
                    extractEmbeddedImages(selected, headerIndex, columnCount);
            List<WorkbookPreviewResponse.RowResponse> rows = new ArrayList<>();
            for (int rowIndex = headerIndex + 1; rowIndex <= selected.getLastRowNum(); rowIndex++) {
                Row row = selected.getRow(rowIndex);
                if (row == null || isBlank(row, formatter)) {
                    continue;
                }
                if (rows.size() >= MAX_DATA_ROWS) {
                    throw new BadRequestException("Excel imports support up to 500 non-empty data rows");
                }
                Map<String, String> values = new LinkedHashMap<>();
                for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                    values.put(
                            CellReference.convertNumToColString(columnIndex),
                            value(row.getCell(columnIndex), formatter)
                    );
                }
                rows.add(new WorkbookPreviewResponse.RowResponse(
                        rowIndex + 1,
                        values,
                        imagesByRow.getOrDefault(rowIndex, List.of())
                ));
            }

            return new WorkbookPreviewResponse(
                    sheets,
                    sheetIndex,
                    headerRow,
                    columns,
                    rows,
                    rows.size()
            );
        } catch (EncryptedDocumentException exception) {
            throw new BadRequestException("Password-protected Excel workbooks are not supported");
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof BadRequestException badRequestException) {
                throw badRequestException;
            }
            throw new BadRequestException("The uploaded file is not a readable Excel workbook");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("An Excel workbook is required");
        }
        if (file.getSize() > MAX_WORKBOOK_BYTES) {
            throw new BadRequestException("Excel workbooks must not exceed 15 MB");
        }
        String fileName = file.getOriginalFilename();
        String normalized = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        if (!normalized.endsWith(".xlsx") && !normalized.endsWith(".xls")) {
            throw new BadRequestException("Only .xlsx and .xls files are supported");
        }
    }

    private int findColumnCount(Sheet sheet, int headerIndex) {
        int columnCount = 0;
        for (int rowIndex = headerIndex; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null && row.getLastCellNum() > columnCount) {
                columnCount = row.getLastCellNum();
            }
        }
        return columnCount;
    }

    private boolean isBlank(Row row, DataFormatter formatter) {
        if (row.getFirstCellNum() < 0) {
            return true;
        }
        for (int index = row.getFirstCellNum(); index < row.getLastCellNum(); index++) {
            if (!value(row.getCell(index), formatter).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String value(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            return "";
        }
        String value = formatter.formatCellValue(cell).trim();
        if (value.length() > MAX_CELL_CHARACTERS) {
            throw new BadRequestException("An Excel cell exceeds the 20,000 character limit");
        }
        return value;
    }

    private Map<Integer, List<WorkbookPreviewResponse.EmbeddedImageResponse>> extractEmbeddedImages(
            Sheet sheet,
            int headerIndex,
            int columnCount
    ) {
        Map<Integer, List<WorkbookPreviewResponse.EmbeddedImageResponse>> imagesByRow = new HashMap<>();
        Drawing<?> drawing = sheet.getDrawingPatriarch();
        if (drawing == null) {
            return imagesByRow;
        }

        long totalBytes = 0;
        int imageCount = 0;
        for (Shape shape : drawing) {
            if (!(shape instanceof Picture picture)) {
                continue;
            }
            ClientAnchor anchor = picture.getClientAnchor();
            if (anchor == null
                    || anchor.getRow1() <= headerIndex
                    || anchor.getRow1() > sheet.getLastRowNum()
                    || anchor.getCol1() < 0
                    || anchor.getCol1() >= columnCount) {
                continue;
            }

            PictureData pictureData = picture.getPictureData();
            if (pictureData == null || !isSupportedImage(pictureData.getMimeType())) {
                continue;
            }
            byte[] data = pictureData.getData();
            totalBytes += data.length;
            imageCount++;
            if (imageCount > MAX_EMBEDDED_IMAGES || totalBytes > MAX_WORKBOOK_BYTES) {
                throw new BadRequestException("Embedded Excel images must not exceed the workbook import limits");
            }

            String columnKey = CellReference.convertNumToColString(anchor.getCol1());
            String extension = pictureData.suggestFileExtension();
            String fileName = "row-" + (anchor.getRow1() + 1)
                    + "-" + columnKey.toLowerCase(Locale.ROOT)
                    + "-" + imageCount
                    + "." + (extension == null || extension.isBlank() ? "png" : extension);
            imagesByRow.computeIfAbsent(anchor.getRow1(), ignored -> new ArrayList<>())
                    .add(new WorkbookPreviewResponse.EmbeddedImageResponse(
                            columnKey,
                            fileName,
                            pictureData.getMimeType(),
                            Base64.getEncoder().encodeToString(data)
                    ));
        }
        return imagesByRow;
    }

    private boolean isSupportedImage(String contentType) {
        return "image/jpeg".equalsIgnoreCase(contentType)
                || "image/png".equalsIgnoreCase(contentType)
                || "image/gif".equalsIgnoreCase(contentType);
    }
}
