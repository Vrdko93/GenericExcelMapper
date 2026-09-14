package com.synergisticit.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.synergisticit.dto.MappingOverrideRequest;
import com.synergisticit.dto.MappingRequest;
import com.synergisticit.dto.MappingResponse;
import com.synergisticit.entity.Mapping;
import com.synergisticit.service.MappingExecutionService;
import com.synergisticit.service.MappingService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mappings")
@RequiredArgsConstructor
public class MappingController {

    private final MappingService mappingService;
    private final MappingExecutionService mappingExecutionService;

    @PostMapping
    public MappingResponse createMapping(
            @RequestBody MappingRequest request) {

        Mapping mapping =
                mappingService.createMapping(request);

        Long aiRecommendedTargetFieldId = null;

        if (mapping.getAiRecommendedTargetField() != null) {
            aiRecommendedTargetFieldId =
                    mapping.getAiRecommendedTargetField().getId();
        }

        return new MappingResponse(
                mapping.getId(),

                mapping.getSourceColumn().getId(),
                mapping.getSourceColumn().getColumnName(),

                mapping.getTargetField().getId(),
                mapping.getTargetField().getName(),

                mapping.getTransformation(),

                mapping.getMappingMethod(),

                mapping.getAiConfidence(),

                aiRecommendedTargetFieldId,
                mapping.getTransformationConfig()
        );
    }
    
    @PutMapping("/{id}")
    public Mapping overrideMapping(@PathVariable Long id, @RequestBody MappingOverrideRequest request) {

        return mappingService.overrideMapping(id, request);
    }
    
    @PostMapping(value = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<Map<String, String>> preview(
            @RequestParam MultipartFile file,
            @RequestParam String sheet,
            @RequestParam Long sourceProfileId)
            throws IOException {

        return mappingExecutionService.preview(
                file,
                sheet,
                sourceProfileId
        );
    }
}
