package io.extremum.sharedmodels.converter;

import io.extremum.sharedmodels.basic.IntegerOrString;
import io.extremum.sharedmodels.converter.TimeFrameConverter;
import io.extremum.sharedmodels.spacetime.TimeFrame;
import io.extremum.sharedmodels.spacetime.TimeFrameDocument;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class TimeFrameConverterTest {
    private final TimeFrameConverter converter = new TimeFrameConverter();

    private final ZonedDateTime start = ZonedDateTime.now().truncatedTo(ChronoUnit.MICROS);
    private final ZonedDateTime end = start.plusSeconds(1);
    private final IntegerOrString duration = new IntegerOrString(millis);

    private static final int millis = 1000;

    @Test
    void copiesWriteThroughFieldsFromTimeFrameDocumentToTimeFrame() {
        TimeFrameDocument document = new TimeFrameDocument();
        document.setStart(start);
        document.setEnd(end);
        document.setDurationMs(millis);

        TimeFrame timeFrame = converter.documentToDto(document);

        assertThat(timeFrame.getStart(), is(start));
        assertThat(timeFrame.getEnd(), is(end));
        assertThat(timeFrame.getDuration(), is(duration));
        assertThat(timeFrame.javaDuration(), is(Duration.ofMillis(millis)));
    }

    @Test
    void copiesWriteThroughFieldsFromTimeFrameToTimeFrameDocument() {
        TimeFrame timeFrame = new TimeFrame();
        timeFrame.setStart(start);
        timeFrame.setEnd(end);
        timeFrame.setDuration(duration);

        TimeFrameDocument document = converter.dtoToDocument(timeFrame);

        assertThat(document.getStart(), is(start));
        assertThat(document.getEnd(), is(end));
        assertThat(document.getDurationMs(), is(millis));
    }

    @Test
    void copiesDurationMillisFromTimeFrameToTimeFrameDocumentIfMillisArePresent() {
        TimeFrame timeFrame = new TimeFrame();
        timeFrame.setDuration(new IntegerOrString(500));

        TimeFrameDocument document = converter.dtoToDocument(timeFrame);

        assertThat(document.getDurationMs(), is(500));
    }

    @Test
    void convertDurationMillisFromTimeFrameToTimeFrameDocumentIfDurationStringIsPresent() {
        TimeFrame timeFrame = new TimeFrame();
        timeFrame.setDuration(new IntegerOrString("5h"));

        TimeFrameDocument document = converter.dtoToDocument(timeFrame);

        int fiveHoursInMillis = (int) Duration.ofHours(5).toMillis();
        assertThat(document.getDurationMs(), is(fiveHoursInMillis));
    }

}