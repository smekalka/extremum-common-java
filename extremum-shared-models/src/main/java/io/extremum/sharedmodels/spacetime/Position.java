package io.extremum.sharedmodels.spacetime;

import io.extremum.sharedmodels.annotation.DocumentationName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

/**
 * Created by vov4a on 07.09.17.
 */
@Data
@DocumentationName("Position")
public class Position {

    @NotNull
    @JsonProperty("timestamp")
    private ZonedDateTime timestamp;

    @NotNull
    @JsonProperty("latitude")
    private Number latitude;

    @NotNull
    @JsonProperty("longitude")
    private Number longitude;

    @JsonProperty("accuracy")
    private Number accuracy;

    @JsonProperty("altitude")
    private Number altitude;

    @JsonProperty("altitudeAccuracy")
    private Number altitudeAccuracy;

    @JsonProperty("heading")
    private Number heading;

    @JsonProperty("speed")
    private Number speed;

    public enum FIELDS {
        timestamp, latitude, longitude, accuracy, altitude, altitudeAccuracy, heading, speed
    }
}
