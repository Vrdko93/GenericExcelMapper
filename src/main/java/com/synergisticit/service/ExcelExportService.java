package com.synergisticit.service;

import com.synergisticit.dto.ValidationError;
import com.synergisticit.entity.TargetField;
import com.synergisticit.repository.TargetFieldRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final MappingExecutionService mappingExecutionService;
    private final MappingValidationService mappingValidationService;
    private final TargetFieldRepository targetFieldRepository;

    public byte[] export(MultipartFile file, String sheetName, Long sourceProfileId, Long templateVersionId) throws IOException {

        // 1. Execute mappings + transformations
        List<Map<String, String>> rows = mappingExecutionService.preview(file, sheetName, sourceProfileId);

        // 2. Validate transformed data
        List<ValidationError> errors = mappingValidationService.validate(rows, templateVersionId);

        if (!errors.isEmpty()) {
            throw new IllegalStateException("Export failed. Validation errors found: " + errors.size());
        }

        // 3. Get official target-field order
        List<TargetField> targetFields = targetFieldRepository.findByTemplateVersionIdOrderBySequence(templateVersionId);

        // 4. Build Excel workbook
        try (Workbook workbook = new XSSFWorkbook(); 
        	 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Mapped Data");

            createHeaderRow(sheet, targetFields);
            createDataRows(sheet, rows, targetFields);

            autoSizeColumns(sheet, targetFields.size());
            workbook.write(outputStream);

            return outputStream.toByteArray();
        }
    }

    private void createHeaderRow(Sheet sheet, List<TargetField> targetFields) {

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < targetFields.size(); i++) {

            Cell cell = headerRow.createCell(i);
            cell.setCellValue(targetFields.get(i).getName());
        }
    }

    private void createDataRows(Sheet sheet, List<Map<String, String>> rows, List<TargetField> targetFields) {

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {

            Map<String, String> data = rows.get(rowIndex);
            Row excelRow = sheet.createRow(rowIndex + 1);

            for (int columnIndex = 0; columnIndex < targetFields.size(); columnIndex++) {

                TargetField field = targetFields.get(columnIndex);
                String value = data.getOrDefault(field.getName(), "");

                Cell cell = excelRow.createCell(columnIndex);
                cell.setCellValue(value);
            }
        }
    }

    private void autoSizeColumns(Sheet sheet, int numberOfColumns) {

        for (int i = 0; i < numberOfColumns; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}