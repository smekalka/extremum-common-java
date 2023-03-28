package io.extremum.everything.reactive.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import io.extremum.sharedmodels.fundamental.CommonResponseDto;

import java.time.ZonedDateTime;

class TestResponseDto extends CommonResponseDto {
    @JsonProperty
    public String name;
    @JsonProperty
    public String model;

    @JsonCreator
    public TestResponseDto() {
    }

    TestResponseDto(String name) {
        this.name = name;

        setId(forName(name));
        setCreated(ZonedDateTime.now());
        setModified(ZonedDateTime.now());
        setVersion(1L);
    }

    private static Descriptor forName(String name) {
        return Descriptor.builder()
                .externalId("external-id-" + name)
                .internalId("internal-id-" + name)
                .modelType("Test")
                .storageType(StandardStorageType.MONGO)
                .build();
    }

    @Override
    public String getModel() {
        return "Test";
    }
}
