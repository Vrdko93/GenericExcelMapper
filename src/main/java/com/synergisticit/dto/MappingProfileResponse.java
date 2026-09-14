package com.synergisticit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MappingProfileResponse {

    private Long id;
    private String name;

    private Long templateVersionId;
    private String templateVersion;

    private Boolean active;
}
