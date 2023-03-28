package io.extremum.sharedmodels.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import io.extremum.sharedmodels.fundamental.CommonResponseDto;

import java.util.Map;

public class UniversalResponseDto extends CommonResponseDto {
    private final Map<String, Object> data;

    public UniversalResponseDto(Map<String, Object> data) {
        this.data = data;
    }

    @JsonAnyGetter
    public Map<String, Object> getData() {
        return data;
    }

    @AttributeGetter
    public Object getAttribute(String attributeName) {
        return getData().get(attributeName);
    }
}