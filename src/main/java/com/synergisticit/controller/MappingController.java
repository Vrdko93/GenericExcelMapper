package com.synergisticit.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
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
import com.synergisticit.dto.MappingReviewResponse;
import com.synergisticit.dto.ValidationError;
import com.synergisticit.entity.Mapping;
import com.synergisticit.service.ExcelExportService;
import com.synergisticit.service.MappingExecutionService;
import com.synergisticit.service.MappingService;
import com.synergisticit.service.MappingValidationService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/mappings")
@RequiredArgsConstructor
public class MappingController {

    private final MappingService mappingService;
    private final MappingExecutionService mappingExecutionService;
    private final MappingValidationService mappingValidationService;
    private final ExcelExportService excelExportService;

    @PostMapping
    public MappingResponse createMapping(@RequestBody MappingRequest request) {

        Mapping mapping = mappingService.createMapping(request);
        Long aiRecommendedTargetFieldId = null;

        if (mapping.getAiRecommendedTargetField() != null) {
            aiRecommendedTargetFieldId = mapping.getAiRecommendedTargetField().getId();
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
    public List<Map<String, String>> preview(@RequestParam MultipartFile file, @RequestParam String sheet, 
    		                                 @RequestParam Long sourceProfileId) throws IOException {

        return mappingExecutionService.preview(file, sheet, sourceProfileId);
    }
    
    @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<ValidationError> validate(@RequestParam MultipartFile file, @RequestParam String sheet,
                                    @RequestParam Long sourceProfileId, @RequestParam Long templateVersionId) throws IOException {

        List<Map<String, String>> rows = mappingExecutionService.preview(file, sheet, sourceProfileId);
        
        return mappingValidationService.validate(rows, templateVersionId);
    }
    
    @PostMapping(value = "/export", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> export(@RequestParam MultipartFile file, @RequestParam String sheet,
                                     @RequestParam Long sourceProfileId, @RequestParam Long templateVersionId) throws IOException {

        byte[] excel = excelExportService.export(file, sheet, sourceProfileId, templateVersionId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"mapped-output.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
    
    @GetMapping("/review")
    public List<MappingReviewResponse> getMappingReview(@RequestParam Long sourceProfileId) {

        List<Mapping> mappings = mappingService.getMappingsBySourceProfile(sourceProfileId);

        return mappings.stream().map(mapping -> {

                    String aiRecommendedTargetFieldName = null;

                    if (mapping.getAiRecommendedTargetField() != null) {
                        aiRecommendedTargetFieldName = mapping.getAiRecommendedTargetField().getName();
                    }

                    return new MappingReviewResponse(
                            mapping.getId(),
                            mapping.getSourceColumn().getColumnName(),
                            mapping.getTargetField().getName(),
                            mapping.getMappingMethod(),
                            aiRecommendedTargetFieldName,
                            mapping.getAiConfidence(),
                            mapping.getTransformation(),
                            mapping.getTransformationConfig()
                    );
                })
                .toList();
    }
}