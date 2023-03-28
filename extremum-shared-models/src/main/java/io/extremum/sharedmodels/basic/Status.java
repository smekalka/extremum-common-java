package io.extremum.sharedmodels.basic;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Status {
    @JsonProperty
    draft,
    @JsonProperty
    active,
    @JsonProperty
    hidden;
}
