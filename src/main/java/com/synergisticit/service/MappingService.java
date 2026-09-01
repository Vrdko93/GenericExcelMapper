package com.synergisticit.service;

import org.springframework.stereotype.Service;
import com.synergisticit.dto.MappingRequest;
import com.synergisticit.entity.Mapping;
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

    	SourceColumn sourceColumn =
    	        sourceColumnRepository.findById(request.getSourceColumnId())
    	        .orElseThrow(() -> new IllegalArgumentException("Source column not found: " + request.getSourceColumnId()));

        TargetField targetField =
                targetFieldRepository.findById(request.getTargetFieldId())
                .orElseThrow(() -> new IllegalArgumentException("Target field not found: " + request.getTargetFieldId()));

        TransformationType transformation = request.getTransformation();

        if (transformation == null) {
            transformation = TransformationType.DIRECT;
        }

        Mapping mapping = new Mapping();

        mapping.setSourceColumn(sourceColumn);
        mapping.setTargetField(targetField);
        mapping.setTransformation(transformation);

        return mappingRepository.save(mapping);
    }
}
