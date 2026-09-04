package com.synergisticit.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class AiMappingResponse {

    private List<AiMapping> mappings;

    @Getter
    @Setter
    public static class AiMapping {

        private Long sourceColumnId;

        private Long targetFieldId;

        private int confidence;

        private String reason;
    }
}
