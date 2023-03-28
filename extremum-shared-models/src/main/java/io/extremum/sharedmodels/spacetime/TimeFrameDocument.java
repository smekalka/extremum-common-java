package io.extremum.sharedmodels.spacetime;

import io.extremum.datetime.PreciseDateTimes;
import io.extremum.sharedmodels.annotation.DocumentationName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.ZonedDateTime;

/**
 * Represents Timeframe for storage in a document-oriented database like Mongo.
 * This means that duration is also stored in milliseconds.
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@DocumentationName("Timeframe")
public class TimeFrameDocument {

    private ZonedDateTime start;
    private Long startMicros;
    private ZonedDateTime end;
    private Long endMicros;
    private int durationMs;

    public ZonedDateTime getStart() {
        return PreciseDateTimes.preciseZonedDateTime(start, startMicros);
    }

    public void setStart(ZonedDateTime preciseStart) {
        PreciseDateTimes.storePreciseZonedDateTime(preciseStart, t -> this.start = t, t -> this.startMicros = t);
    }

    public ZonedDateTime getEnd() {
        return PreciseDateTimes.preciseZonedDateTime(end, endMicros);
    }

    public void setEnd(ZonedDateTime preciseEnd) {
        PreciseDateTimes.storePreciseZonedDateTime(preciseEnd, t -> this.end = t, t -> this.endMicros = t);
    }

    public enum FIELDS {
        start, end, duration
    }
}
