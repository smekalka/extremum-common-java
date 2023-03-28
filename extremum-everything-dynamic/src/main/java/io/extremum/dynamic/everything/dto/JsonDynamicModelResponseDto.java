package io.extremum.dynamic.everything.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldResource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static io.extremum.sharedmodels.basic.Model.FIELDS.created;
import static io.extremum.sharedmodels.basic.Model.FIELDS.createdBy;
import static io.extremum.sharedmodels.basic.Model.FIELDS.model;
import static io.extremum.sharedmodels.basic.Model.FIELDS.modified;
import static io.extremum.sharedmodels.basic.Model.FIELDS.modifiedBy;
import static io.extremum.sharedmodels.basic.Model.FIELDS.version;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

@Slf4j
@Getter
@Setter
@AllArgsConstructor
@JsonFilter("dynamicModelFilter")
public class JsonDynamicModelResponseDto implements DynamicModelResponseDto<Map<String, Object>> {
    private Map<String, Object> data;

    @JsonProperty("@uuid")
    private Descriptor id;

    public JsonDynamicModelResponseDto() {
        System.out.println();
    }

    @JsonProperty("@version")
    public Long getVersion() {
        return extract(version.name(), Long.class).orElse(null);
    }

    @JsonProperty("@created")
    public ZonedDateTime getCreated() {
        return extract(created.name(), ZonedDateTime.class)
                .orElseThrow(fieldNotPresented(created));
    }

    @JsonProperty("@modified")
    public ZonedDateTime getModified() {
        return extract(modified.name(), ZonedDateTime.class)
                .orElseThrow(fieldNotPresented(modified));
    }

    @JsonProperty("@type")
    public String getModel() {
        return extract(model.name(), String.class)
                .orElseThrow(fieldNotPresented(model));
    }

    private Supplier<RuntimeException> fieldNotPresented(Model.FIELDS field) {
        return () -> {
            String msg = format("Field %s is not presented in model %s", field, this);
            log.error(msg);
            return new RuntimeException(msg);
        };
    }

    private <T> Optional<T> extract(String field, Class<T> classType) {
        return ofNullable(data.get(field))
                .filter(classType::isInstance)
                .map(classType::cast);
    }

    @JsonAnyGetter
    public Map<String, Object> getData() {
        return data;
    }
}
