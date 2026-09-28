package com.synergisticit.dto;

import com.synergisticit.entity.MappingMethod;
import com.synergisticit.entity.TransformationType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MappingReviewResponse {

    private Long mappingId;
    private String sourceColumnName;

    private String targetFieldName;
    private MappingMethod mappingMethod;

    private String aiRecommendedTargetFieldName;
    private Integer aiConfidence;

    private TransformationType transformation;
    private String transformationConfig;
}
