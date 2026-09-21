package com.synergisticit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ValidationError {

    private int rowNumber;
    private String targetField;
    private String message;
}
