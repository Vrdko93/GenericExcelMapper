package com.synergisticit.dto;

import com.synergisticit.entity.TransformationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MappingOverrideRequest {

    private Long targetFieldId;
    private TransformationType transformation;
}
