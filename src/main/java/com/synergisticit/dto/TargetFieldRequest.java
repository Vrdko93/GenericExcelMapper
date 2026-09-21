package com.synergisticit.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TargetFieldRequest {

    private Long templateVersionId;
    private String key;
    private String name;

    private String description;
    private Integer sequence;
    private String dataType;

    private Boolean mandatory;
    private String defaultValue;
}
