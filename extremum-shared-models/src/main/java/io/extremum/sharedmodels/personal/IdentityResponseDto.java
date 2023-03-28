package io.extremum.sharedmodels.personal;

import io.extremum.sharedmodels.annotation.DocumentationName;
import io.extremum.sharedmodels.basic.IdOrObject;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.fundamental.CommonResponseDto;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@DocumentationName({"Agent", "Identity"})
public class IdentityResponseDto extends CommonResponseDto {
    public static final String MODEL_NAME = "Identity";

    private boolean verified;
    private Locale locale;
    private String timezone;
    private IdOrObject<Descriptor, PersonResponseDto> person;

    private final Map<String, Object> extensions = new LinkedHashMap<>();

    public void putExtension(String key, Object value) {
        extensions.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getExtensions() {
        return extensions;
    }

    @Override
    public String getModel() {
        return MODEL_NAME;
    }
}
