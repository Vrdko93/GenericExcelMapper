package com.synergisticit.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

import com.synergisticit.dto.ValidationError;
import com.synergisticit.entity.TargetField;
import com.synergisticit.repository.TargetFieldRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MappingValidationService {

    private final TargetFieldRepository targetFieldRepository;

    public List<ValidationError> validate(List<Map<String, String>> rows, Long templateVersionId) {

        List<ValidationError> errors = new ArrayList<>();
        List<TargetField> targetFields = targetFieldRepository.findByTemplateVersionIdOrderBySequence(templateVersionId);

        for (int i = 0; i < rows.size(); i++) {

            Map<String, String> row =
                    rows.get(i);

            int rowNumber = i + 2;

            for (TargetField field : targetFields) {

                String value =
                        row.get(field.getName());

                if (Boolean.TRUE.equals(field.getMandatory())
                        && (value == null || value.isBlank())) {

                    errors.add(
                            new ValidationError(
                                    rowNumber,
                                    field.getName(),
                                    "Mandatory field is missing"
                            )
                    );

                    continue;
                }

                if (value == null || value.isBlank()) {
                    continue;
                }

                if (!isValidType(
                        value,
                        field.getDataType())) {

                    errors.add(
                            new ValidationError(
                                    rowNumber,
                                    field.getName(),
                                    "Invalid value for type "
                                            + field.getDataType()
                            )
                    );
                }
            }
        }

        return errors;
    }

    private boolean isValidType(
            String value,
            String dataType) {

        if (dataType == null) {
            return true;
        }

        try {

            return switch (
                    dataType.toUpperCase()) {

                case "STRING" ->
                        true;

                case "INTEGER" -> {
                    Integer.parseInt(
                            value.replace(",", "")
                    );
                    yield true;
                }

                case "DECIMAL" -> {
                    Double.parseDouble(
                            value.replace(",", "")
                    );
                    yield true;
                }

                case "BOOLEAN" ->
                        value.equalsIgnoreCase("true")
                                || value.equalsIgnoreCase("false")
                                || value.equalsIgnoreCase("yes")
                                || value.equalsIgnoreCase("no");

                case "DATE" -> {
                    LocalDate.parse(value);
                    yield true;
                }

                case "EMAIL" ->
                        value.matches(
                                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
                        );

                default ->
                        true;
            };

        } catch (Exception e) {
            return false;
        }
    }
}
