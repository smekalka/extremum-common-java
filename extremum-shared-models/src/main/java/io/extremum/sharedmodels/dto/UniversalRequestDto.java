package io.extremum.sharedmodels.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public final class UniversalRequestDto implements RequestDto {

    private Map<String, Object> data;
    private String id;

    public UniversalRequestDto(Map<String, Object> data) {
        this.data = data;
    }

    public UniversalRequestDto(String id) {
        this.id = id;
    }

    @JsonAnyGetter
    public Map<String, Object> getData() {
        return this.data;
    }

    @JsonAnySetter
    public void addOtherInfo(String propertyKey, Object value) {
        if (this.data == null) {
            this.data = new HashMap<>();
        }
        this.data.put(propertyKey, value);
    }

}
