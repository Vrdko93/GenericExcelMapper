package com.synergisticit.service;

import com.synergisticit.entity.Mapping;
import com.synergisticit.entity.TransformationType;
import com.synergisticit.repository.MappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MappingExecutionService {

    private final ExcelService excelService;
    private final MappingRepository mappingRepository;

    public List<Map<String, String>> preview(
            MultipartFile file,
            String sheetName,
            Long sourceProfileId) throws IOException {

        List<Map<String, String>> sourceRows =
                excelService.readRows(file, sheetName);

        List<Mapping> mappings =
                mappingRepository
                        .findBySourceColumnSourceProfileId(sourceProfileId);

        List<Map<String, String>> previewRows =
                new ArrayList<>();

        for (Map<String, String> sourceRow : sourceRows) {

            Map<String, String> targetRow =
                    new LinkedHashMap<>();

            for (Mapping mapping : mappings) {

                String sourceColumnName =
                        mapping.getSourceColumn().getColumnName();

                String targetFieldName =
                        mapping.getTargetField().getName();

                String sourceValue =
                        sourceRow.get(sourceColumnName);

                String transformedValue =
                        applyTransformation(
                                sourceValue,
                                mapping.getTransformation(),
                                mapping.getTransformationConfig()
                        );

                targetRow.put(
                        targetFieldName,
                        transformedValue
                );
            }

            previewRows.add(targetRow);
        }

        return previewRows;
    }

    private String applyTransformation(
            String value,
            TransformationType transformation,
            String config) {

        if (transformation == null) {
            transformation = TransformationType.DIRECT;
        }

        return switch (transformation) {

            case DIRECT ->
                    value == null ? "" : value;

            case TRIM ->
                    value == null ? "" : value.trim();

            case UPPERCASE ->
                    value == null ? "" : value.toUpperCase();

            case LOWERCASE ->
                    value == null ? "" : value.toLowerCase();

            case CONSTANT ->
                    config == null ? "" : config;

            case DEFAULT -> {
                if (value == null || value.isBlank()) {
                    yield config == null ? "" : config;
                }

                yield value;
            }

            case DATE_FORMAT ->
                    formatDate(value, config);
                    
            case CURRENCY_TO_NUMBER ->
            		currencyToNumber(value);
        };
    }

    private String formatDate(
            String value,
            String config) {

        if (value == null || value.isBlank()) {
            return "";
        }

        if (config == null || config.isBlank()) {
            return value;
        }

        String[] parts =
                config.split("\\s*->\\s*");

        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Invalid DATE_FORMAT config: " + config
            );
        }

        String sourcePattern =
                parts[0].trim();

        String targetPattern =
                parts[1].trim();

        DateTimeFormatter targetFormatter =
                DateTimeFormatter.ofPattern(targetPattern);

        LocalDate date;

        if (sourcePattern.contains("yy")
                && !sourcePattern.contains("yyyy")) {

            DateTimeFormatter sourceFormatter =
                    new DateTimeFormatterBuilder()
                            .appendPattern(
                                    sourcePattern.replace(
                                            "yy",
                                            ""
                                    )
                            )
                            .appendValueReduced(
                                    ChronoField.YEAR,
                                    2,
                                    2,
                                    1900
                            )
                            .toFormatter();

            date = LocalDate.parse(
                    value,
                    sourceFormatter
            );

        } else {

            DateTimeFormatter sourceFormatter =
                    DateTimeFormatter.ofPattern(
                            sourcePattern
                    );

            date = LocalDate.parse(
                    value,
                    sourceFormatter
            );
        }

        return date.format(
                targetFormatter
        );
    }
    
    private String currencyToNumber(String value) {

        if (value == null || value.isBlank()) {
            return "";
        }

        String cleaned = value
                .trim()
                .replaceAll("[,$£€¥\\s]", "");

        if (cleaned.isBlank()) {
            return "";
        }

        try {

            BigDecimal number =
                    new BigDecimal(cleaned);

            return number.stripTrailingZeros()
                    .toPlainString();

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Invalid currency value: " + value
            );
        }
    }
}
