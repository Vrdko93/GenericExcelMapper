package com.synergisticit.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.synergisticit.dto.ValidationError;
import com.synergisticit.entity.TargetField;
import com.synergisticit.entity.ValidationRule;
import com.synergisticit.repository.TargetFieldRepository;
import com.synergisticit.repository.ValidationRuleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MappingValidationService {

    private final TargetFieldRepository targetFieldRepository;
    private final ValidationRuleRepository validationRuleRepository;

    public List<ValidationError> validate(List<Map<String, String>> rows, Long templateVersionId) {

        List<ValidationError> errors = new ArrayList<>();
        List<TargetField> targetFields = targetFieldRepository.findByTemplateVersionIdOrderBySequence(templateVersionId);

        List<ValidationRule> validationRules = validationRuleRepository
                        .findByTargetFieldTemplateVersionIdAndActiveTrueOrderBySequence(templateVersionId);

        Map<Long, List<ValidationRule>> rulesByField = validationRules.stream()
                        .collect(Collectors.groupingBy(rule -> rule.getTargetField().getId()));

        for (int i = 0; i < rows.size(); i++) {

            Map<String, String> row = rows.get(i);
            int rowNumber = i + 2;

            for (TargetField field : targetFields) {

                String value = row.get(field.getName());

                List<ValidationRule> fieldRules = rulesByField.getOrDefault(field.getId(), List.of());

                boolean requiredByRule =
                        fieldRules.stream()
                                .anyMatch(rule ->
                                        "REQUIRED".equalsIgnoreCase(
                                                rule.getRuleType()
                                        )
                                );

                boolean blank =
                        value == null ||
                        value.isBlank();

                // Required either by TargetField.mandatory
                // or by a configured REQUIRED rule.
                if (blank &&
                        (Boolean.TRUE.equals(field.getMandatory())
                                || requiredByRule)) {

                    errors.add(
                            new ValidationError(
                                    rowNumber,
                                    field.getName(),
                                    getRequiredErrorMessage(
                                            fieldRules
                                    )
                            )
                    );

                    continue;
                }

                // Optional blank values are valid.
                if (blank) {
                    continue;
                }

                // Basic data-type validation first.
                if (!isValidType(
                        value,
                        field.getDataType())) {

                    errors.add(
                            new ValidationError(
                                    rowNumber,
                                    field.getName(),
                                    "Invalid " +
                                            field.getDataType() +
                                            " value"
                            )
                    );

                    continue;
                }

                // Configured validation rules.
                for (ValidationRule rule : fieldRules) {

                    // REQUIRED was already handled above.
                    if ("REQUIRED".equalsIgnoreCase(
                            rule.getRuleType())) {
                        continue;
                    }

                    String ruleError =
                            validateRule(
                                    value,
                                    rule
                            );

                    if (ruleError != null) {

                        errors.add(
                                new ValidationError(
                                        rowNumber,
                                        field.getName(),
                                        ruleError
                                )
                        );
                    }
                }
            }
        }

        return errors;
    }

    private boolean isValidType(String value, String dataType) {

        if (dataType == null) {
            return true;
        }

        try {

            return switch (dataType.toUpperCase()) {

                case "STRING" ->
                        true;

                case "INTEGER" -> {
                    Integer.parseInt(value.replace(",", ""));
                    yield true;
                }

                case "DECIMAL" -> {
                    Double.parseDouble(value.replace(",", ""));
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
                        value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

                default ->
                        true;
            };

        } catch (Exception e) {
            return false;
        }
    }
    
    private String validateRule(String value, ValidationRule rule) {

        String ruleType = rule.getRuleType();
        String ruleValue =rule.getRuleValue();

        boolean valid = true;

        try {

            switch (ruleType) {

                case "REQUIRED" -> {
                	
                    valid = value != null && !value.isBlank();
                }

                case "REGEX" -> {

                    if (value != null && !value.isBlank()) {

                        valid = value.matches(ruleValue);
                    }
                }

                case "MIN_LENGTH" -> {

                    if (value != null && !value.isBlank()) {

                        valid = value.length() >= Integer.parseInt(ruleValue);
                    }
                }

                case "MAX_LENGTH" -> {

                    if (value != null && !value.isBlank()) {

                        valid = value.length() <= Integer.parseInt(ruleValue);
                    }
                }

                case "MIN_VALUE" -> {

                    if (value != null && !value.isBlank()) {

                        BigDecimal actual = new BigDecimal(value);
                        BigDecimal minimum = new BigDecimal(ruleValue);

                        valid = actual.compareTo(minimum) >= 0;
                    }
                }

                case "MAX_VALUE" -> {

                    if (value != null && !value.isBlank()) {

                        BigDecimal actual = new BigDecimal(value);
                        BigDecimal maximum = new BigDecimal(ruleValue);

                        valid = actual.compareTo(maximum) <= 0;
                    }
                }

                default -> {
                    return null;
                }
            }

        } catch (Exception e) {

            return "Invalid validation rule configuration.";
        }

        if (valid) {
            return null;
        }

        if (rule.getErrorMessage() != null && !rule.getErrorMessage().isBlank()) {
            return rule.getErrorMessage();
        }

        return "Validation rule failed: " + ruleType;
    }
    
    private String getRequiredErrorMessage(List<ValidationRule> rules) {

        return rules.stream()
                .filter(rule -> "REQUIRED".equalsIgnoreCase(rule.getRuleType()))
                .map(ValidationRule::getErrorMessage)
                .filter(message -> message != null && !message.isBlank())
                .findFirst()
                .orElse("Mandatory field is missing");
    }
}
