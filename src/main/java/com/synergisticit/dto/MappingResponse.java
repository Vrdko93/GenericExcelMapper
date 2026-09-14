package com.synergisticit.dto;

import com.synergisticit.entity.MappingMethod;
import com.synergisticit.entity.TransformationType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MappingResponse {

    private Long id;

    private Long sourceColumnId;
    private String sourceColumnName;

    private Long targetFieldId;
    private String targetFieldName;

    private TransformationType transformation;
    private MappingMethod mappingMethod;

    private Integer aiConfidence;
    private Long aiRecommendedTargetFieldId;
    
    private String transformationConfig;
}
