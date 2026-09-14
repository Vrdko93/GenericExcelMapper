package com.synergisticit.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MappingProfileRequest {

    private String name;

    private Long sourceProfileId;

    private Long templateVersionId;
}
