package com.synergisticit.service;

import java.util.List;

import org.springframework.stereotype.Service;
import com.synergisticit.dto.MappingOverrideRequest;
import com.synergisticit.dto.MappingRequest;
import com.synergisticit.entity.Mapping;
import com.synergisticit.entity.MappingMethod;
import com.synergisticit.entity.SourceColumn;
import com.synergisticit.entity.TargetField;
import com.synergisticit.entity.TransformationType;
import com.synergisticit.repository.MappingRepository;
import com.synergisticit.repository.SourceColumnRepository;
import com.synergisticit.repository.TargetFieldRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MappingService {

    private final MappingRepository mappingRepository;
    private final SourceColumnRepository sourceColumnRepository;
    private final TargetFieldRepository targetFieldRepository;

    public Mapping createMapping(MappingRequest request) {

        SourceColumn sourceColumn = sourceColumnRepository.findById(request.getSourceColumnId())
                        .orElseThrow(() -> new IllegalArgumentException("Source column not found: " + request.getSourceColumnId()));

        TargetField targetField = targetFieldRepository.findById(request.getTargetFieldId())
                        .orElseThrow(() -> new IllegalArgumentException("Target field not found: " + request.getTargetFieldId()));

        
        if (mappingRepository.existsBySourceColumnId(sourceColumn.getId())) {
            throw new IllegalArgumentException("Source column is already mapped: " + sourceColumn.getColumnName());
        }

        Long sourceProfileId = sourceColumn.getSourceProfile().getId();

        if (mappingRepository.existsBySourceColumnSourceProfileIdAndTargetFieldId(sourceProfileId, targetField.getId())) {

            throw new IllegalArgumentException("Target field is already mapped for this source profile: " + targetField.getName());
        }

        TransformationType transformation = request.getTransformation();

        if (transformation == null) {
            transformation = TransformationType.DIRECT;
        }
        
        MappingMethod mappingMethod = request.getMappingMethod();

        if (mappingMethod == null) {
            mappingMethod = MappingMethod.MANUAL;
        }
        
        TargetField aiRecommendedTargetField = null;

        if (request.getAiRecommendedTargetFieldId() != null) {

            aiRecommendedTargetField = targetFieldRepository.findById(request.getAiRecommendedTargetFieldId())
                    .orElseThrow(() -> new IllegalArgumentException("AI recommended target field not found: "
                    												+ request.getAiRecommendedTargetFieldId()));
        }

        Mapping mapping = new Mapping();
        mapping.setSourceColumn(sourceColumn);
        mapping.setTargetField(targetField);
        mapping.setTransformation(transformation);
        mapping.setMappingMethod(mappingMethod);
        mapping.setAiConfidence(request.getAiConfidence());
        mapping.setAiRecommendedTargetField(aiRecommendedTargetField);
        mapping.setTransformationConfig(request.getTransformationConfig());

        return mappingRepository.save(mapping);
    }
    
    public Mapping overrideMapping(Long mappingId, MappingOverrideRequest request) {

        Mapping mapping = mappingRepository.findById(mappingId)
                        .orElseThrow(() -> new IllegalArgumentException("Mapping not found: " + mappingId));

        TargetField newTargetField = targetFieldRepository.findById(request.getTargetFieldId())
                        .orElseThrow(() -> new IllegalArgumentException("Target field not found: " 
                        											+ request.getTargetFieldId()));

        Long sourceProfileId = mapping.getSourceColumn().getSourceProfile().getId();

        if (!mapping.getTargetField().getId().equals(newTargetField.getId()) 
        		&& mappingRepository.existsBySourceColumnSourceProfileIdAndTargetFieldId(sourceProfileId, newTargetField.getId())) {

            throw new IllegalArgumentException("Target field is already mapped for this source profile: " + newTargetField.getName());
        }

        mapping.setTargetField(newTargetField);

        if (request.getTransformation() != null) {
            mapping.setTransformation(request.getTransformation());
        }

        mapping.setMappingMethod(MappingMethod.MANUAL);

        return mappingRepository.save(mapping);
    }
    
    public List<Mapping> getMappingsBySourceProfile(Long sourceProfileId) {

        return mappingRepository.findBySourceColumnSourceProfileId(sourceProfileId);
    }
}
