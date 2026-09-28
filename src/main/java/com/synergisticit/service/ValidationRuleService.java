package com.synergisticit.service;

import org.springframework.stereotype.Service;
import com.synergisticit.entity.ValidationRule;
import com.synergisticit.repository.ValidationRuleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidationRuleService {

    private final ValidationRuleRepository validationRuleRepository;

    public ValidationRule updateRule(Long id, ValidationRule request) {

        ValidationRule rule = validationRuleRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Validation rule not found: " + id));

        rule.setRuleType(request.getRuleType());
        rule.setRuleValue(request.getRuleValue());
        rule.setErrorMessage(request.getErrorMessage());
        rule.setSequence(request.getSequence());

        rule.setActive(request.getActive() != null ? request.getActive() : true);

        return validationRuleRepository.save(rule);
    }

    public void deleteRule(Long id) {

        ValidationRule rule = validationRuleRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Validation rule not found: " + id));

        validationRuleRepository.delete(rule);
    }
}
