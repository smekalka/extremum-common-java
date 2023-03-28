package io.extremum.watch.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

/**
 * @author rpuch
 */
@RequiredArgsConstructor
public class ModelMetadataResponseDto {
    @JsonProperty
    private final String id;
    @JsonProperty
    private final String model;
    @JsonProperty
    private final ZonedDateTime created;
    @JsonProperty
    private final ZonedDateTime modified;
    @JsonProperty
    private final Long version;
}
