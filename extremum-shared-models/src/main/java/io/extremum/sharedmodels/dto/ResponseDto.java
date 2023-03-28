package io.extremum.sharedmodels.dto;

import io.extremum.sharedmodels.descriptor.Descriptor;

import java.time.ZonedDateTime;

/**
 * Base interface describes a response DTO
 */
public interface ResponseDto extends Dto {
    Descriptor getId();

    Long getVersion();

    ZonedDateTime getCreated();

    ZonedDateTime getModified();

    String getModel();
}
