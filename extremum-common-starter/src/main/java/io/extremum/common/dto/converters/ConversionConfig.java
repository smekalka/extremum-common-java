package io.extremum.common.dto.converters;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConversionConfig {
    private final boolean expand;
    private final String model;

    public static ConversionConfig defaults() {
        return ConversionConfig.builder().build();
    }
}
