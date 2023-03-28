package io.extremum.sharedmodels.spacetime;

import io.extremum.sharedmodels.annotation.DocumentationName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * Created by vov4a on 07.09.17.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@DocumentationName("Coordinates")
public class Coordinates {

    @NotNull
    @JsonProperty("latitude")
    private Double latitude;

    @NotNull
    @JsonProperty("longitude")
    private Double longitude;

    public enum FIELDS {
        latitude, longitude
    }
}
