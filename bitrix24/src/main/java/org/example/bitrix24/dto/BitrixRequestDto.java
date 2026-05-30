package org.example.bitrix24.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class BitrixRequestDto {

    @JsonProperty("entityTypeId")
    private Integer entityTypeId;

    @JsonProperty("fields")
    private Map<String, Object> fields;
}