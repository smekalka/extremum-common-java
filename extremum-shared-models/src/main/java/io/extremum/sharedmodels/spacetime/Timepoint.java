package io.extremum.sharedmodels.spacetime;

import io.extremum.sharedmodels.basic.StringOrObject;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class Timepoint {
    public Timepoint(@NonNull String timestamp) {
        this.timestamp = timestamp;
    }
    public Timepoint(@NonNull String timestamp, StringOrObject<Frame> frame) {
        this.timestamp = timestamp;
        this.frame = frame;
    }

    private final String timestamp;
    private StringOrObject<Frame> frame;
}
