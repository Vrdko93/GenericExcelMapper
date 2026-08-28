package com.synergisticit.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synergisticit.entity.TargetField;
import com.synergisticit.entity.Template;
import com.synergisticit.entity.TemplateVersion;
import com.synergisticit.entity.ValidationRule;
import com.synergisticit.repository.TargetFieldRepository;
import com.synergisticit.repository.TemplateRepository;
import com.synergisticit.repository.TemplateVersionRepository;
import com.synergisticit.repository.ValidationRuleRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateRepository templateRepository;
    private final TemplateVersionRepository templateVersionRepository;
    private final TargetFieldRepository targetFieldRepository;
    private final ValidationRuleRepository validationRuleRepository;

    @GetMapping
    public List<Template> getAllTemplates() {
        return templateRepository.findAll();
    }

    @GetMapping("/{id}")
    public Template getTemplateById(@PathVariable Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
    }

    @PostMapping
    public Template createTemplate(@RequestBody Template template) {
        template.setStatus("DRAFT");
        return templateRepository.save(template);
    }
    
    @PostMapping("/{templateId}/versions")
    public TemplateVersion createVersion(@PathVariable Long templateId, @RequestBody TemplateVersion version) {

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        version.setTemplate(template);
        version.setActive(false);

        return templateVersionRepository.save(version);
    }

    @GetMapping("/{templateId}/versions")
    public List<TemplateVersion> getVersions(@PathVariable Long templateId) {

        return templateVersionRepository.findByTemplateId(templateId);
    }
    
    @PostMapping("/versions/{versionId}/fields")
    public TargetField createTargetField(@PathVariable Long versionId, @RequestBody TargetField field) {

        TemplateVersion version = templateVersionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Template version not found"));

        field.setTemplateVersion(version);

        return targetFieldRepository.save(field);
    }
    
    @GetMapping("/versions/{versionId}/fields")
    public List<TargetField> getTargetFields(@PathVariable Long versionId) {
    	
        return targetFieldRepository.findByTemplateVersionIdOrderBySequence(versionId);
    }
    
    @PostMapping("/fields/{fieldId}/validation-rules")
    public ValidationRule createValidationRule(@PathVariable Long fieldId, @RequestBody ValidationRule rule) {

        TargetField field = targetFieldRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Target field not found"));

        rule.setTargetField(field);

        if (rule.getActive() == null) {
            rule.setActive(true);
        }

        return validationRuleRepository.save(rule);
    }
    
    @GetMapping("/fields/{fieldId}/validation-rules")
    public List<ValidationRule> getValidationRules(@PathVariable Long fieldId) {

        return validationRuleRepository.findByTargetFieldIdOrderBySequence(fieldId);
    }
}
