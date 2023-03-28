package io.extremum.sharedmodels.converter;

import io.extremum.sharedmodels.basic.IntegerOrString;
import io.extremum.sharedmodels.spacetime.TimeFrame;
import io.extremum.sharedmodels.spacetime.TimeFrameDocument;

public class TimeFrameConverter {
    public TimeFrame documentToDto(TimeFrameDocument document) {
        TimeFrame timeFrame = new TimeFrame();

        timeFrame.setStart(document.getStart());
        timeFrame.setEnd(document.getEnd());
        timeFrame.setDuration(new IntegerOrString(document.getDurationMs()));

        return timeFrame;
    }

    public TimeFrameDocument dtoToDocument(TimeFrame timeFrame) {
        TimeFrameDocument document = new TimeFrameDocument();

        document.setStart(timeFrame.getStart());
        document.setEnd(timeFrame.getEnd());

        if (timeFrame.getDuration() != null) {
            document.setDurationMs((int) timeFrame.javaDuration().toMillis());
        }

        return document;
    }
}
