package com.synergisticit.dto;

import com.synergisticit.entity.MappingMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MappingSuggestion {

    private Long sourceColumnId;
    private String sourceColumnName;
    private String sourceDataType;

    private Long targetFieldId;
    private String targetFieldName;
    
    private String targetFieldKey;
    private String targetDataType;

    private int confidence;
    private MappingMethod mappingMethod;
}