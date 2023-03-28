package io.extremum.sharedmodels.spacetime;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
public class Frame {
    public Frame(@NonNull Integer num, @NonNull Number rate) {
        this.num = num;
        this.rate = rate;
    }

    private final Integer num;

    private final Number rate;

    @Setter
    private Number base;
}
