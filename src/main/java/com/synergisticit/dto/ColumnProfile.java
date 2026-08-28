package com.synergisticit.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ColumnProfile {

    private String columnName;
    private String dataType;
    private List<String> sampleValues;
}
