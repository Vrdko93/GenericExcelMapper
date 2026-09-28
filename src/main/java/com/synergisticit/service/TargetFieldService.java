package com.synergisticit.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.synergisticit.dto.TargetFieldRequest;
import com.synergisticit.entity.TargetField;
import com.synergisticit.entity.TemplateVersion;
import com.synergisticit.repository.MappingProfileEntryRepository;
import com.synergisticit.repository.MappingRepository;
import com.synergisticit.repository.TargetFieldRepository;
import com.synergisticit.repository.TemplateVersionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TargetFieldService {

    private final TargetFieldRepository targetFieldRepository;
    private final TemplateVersionRepository templateVersionRepository;
    private final MappingRepository mappingRepository;
    private final MappingProfileEntryRepository mappingProfileEntryRepository;

    public List<TargetField> getTargetFields(Long templateVersionId) {
    	
        return targetFieldRepository.findByTemplateVersionIdOrderBySequence(templateVersionId);
    }

    public TargetField createTargetField(TargetFieldRequest request) {

        TemplateVersion templateVersion = templateVersionRepository
                        .findById(request.getTemplateVersionId())
                        .orElseThrow(() -> new IllegalArgumentException("Template version not found: " 
                        												+ request.getTemplateVersionId()));

        TargetField targetField = new TargetField();

        targetField.setTemplateVersion(templateVersion);
        targetField.setKey(request.getKey());

        targetField.setName(request.getName());
        targetField.setDescription(request.getDescription());

        Integer nextSequence = targetFieldRepository
        		        .findTopByTemplateVersionIdOrderBySequenceDesc(request.getTemplateVersionId())
                        .map(field -> field.getSequence() + 1)
                        .orElse(1);

        targetField.setSequence(nextSequence);
        targetField.setDataType(request.getDataType());

        targetField.setMandatory(request.getMandatory() != null ? request.getMandatory() : false);
        targetField.setDefaultValue(request.getDefaultValue());

        return targetFieldRepository.save(targetField);
    }
    
    public TargetField updateTargetField(Long id, TargetFieldRequest request) {

        TargetField targetField = targetFieldRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Target field not found: " + id));

        targetField.setKey(request.getKey());
        targetField.setName(request.getName());
        targetField.setDescription(request.getDescription());
       
        targetField.setDataType(request.getDataType());

        targetField.setMandatory(request.getMandatory() != null ? request.getMandatory() : false);
        targetField.setDefaultValue(request.getDefaultValue());

        return targetFieldRepository.save(targetField);
    }
    
    @Transactional
    public void deleteTargetField(Long id) {

        TargetField targetField = targetFieldRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Target field not found: " + id));

        if (mappingRepository.existsByTargetFieldId(id)) {
        	
            throw new IllegalStateException("Target field cannot be deleted because it is used by an existing mapping.");
        }

        if (mappingProfileEntryRepository.existsByTargetFieldId(id)) {

            throw new IllegalStateException("Target field cannot be deleted because it is used by a mapping profile.");
        }
        
        if (mappingRepository.existsByAiRecommendedTargetFieldId(id)) {

            throw new IllegalStateException("Target field cannot be deleted because it is referenced by an AI recommendation.");
        }
        
        Long templateVersionId = targetField.getTemplateVersion().getId();

        targetFieldRepository.delete(targetField);
        targetFieldRepository.flush();

        normalizeSequences(templateVersionId);
    }
    
    @Transactional
    public void moveUp(Long id) {

        TargetField current = targetFieldRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Target field not found: " + id));

        Long templateVersionId = current.getTemplateVersion().getId();
        List<TargetField> fields = targetFieldRepository.findByTemplateVersionIdOrderBySequence(templateVersionId);

        int currentIndex = -1;

        for (int i = 0; i < fields.size(); i++) {

            if (fields.get(i).getId().equals(id)) {
            	
                currentIndex = i;
                break;
            }
        }

        // Already first
        if (currentIndex <= 0) {
            return;
        }

        TargetField previous = fields.get(currentIndex - 1);
        swapSequence(current, previous);
    }
    
    @Transactional
    public void moveDown(Long id) {

        TargetField current = targetFieldRepository
                        .findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Target field not found: " + id));

        Long templateVersionId = current.getTemplateVersion().getId();
        List<TargetField> fields = targetFieldRepository.findByTemplateVersionIdOrderBySequence(templateVersionId);

        int currentIndex = -1;

        for (int i = 0; i < fields.size(); i++) {

            if (fields.get(i).getId().equals(id)) {
            	
                currentIndex = i;
                break;
            }
        }

        // Already last
        if (currentIndex == -1 || currentIndex >= fields.size() - 1) {
            return;
        }

        TargetField next = fields.get(currentIndex + 1);
        swapSequence(current, next);
    }
    
    private void swapSequence(TargetField first, TargetField second) {

        Integer firstSequence = first.getSequence();
        first.setSequence(second.getSequence());
        second.setSequence( firstSequence);

        targetFieldRepository.save(first);
        targetFieldRepository.save(second);
    }
    
    private void normalizeSequences(Long templateVersionId) {

        List<TargetField> fields = targetFieldRepository.findByTemplateVersionIdOrderBySequence(templateVersionId);

        for (int i = 0; i < fields.size(); i++) {

            TargetField field = fields.get(i);
            int expectedSequence = i + 1;

            if (!field.getSequence().equals(expectedSequence)) {
                field.setSequence(expectedSequence);
            }
        }

        targetFieldRepository.saveAll(fields);
    }
}
