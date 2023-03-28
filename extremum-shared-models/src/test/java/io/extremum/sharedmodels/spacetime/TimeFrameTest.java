package io.extremum.sharedmodels.spacetime;

import io.extremum.sharedmodels.basic.IntegerOrString;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class TimeFrameTest {
    @Test
    void parseDuration() {
        assertEquals(Duration.ofMillis(10), TimeFrame.parseDuration("10 ms"));
        assertEquals(Duration.ofSeconds(10), TimeFrame.parseDuration("10s"));
        assertEquals(Duration.ofMinutes(10), TimeFrame.parseDuration(" 10m"));
        assertEquals(Duration.ofHours(10), TimeFrame.parseDuration(" 10h "));
        assertEquals(Duration.ofDays(10), TimeFrame.parseDuration(" 10 d "));
        assertEquals(Duration.ofDays(10).plusHours(5).plusMinutes(4).plusSeconds(3).plusMillis(2),
                TimeFrame.parseDuration(" 10 d 5h 4 m 3 s 2ms 1ns"));
    }

    @Test
    void javaDuration() {
        TimeFrame tf = new TimeFrame();
        assertNull(tf.javaDuration());

        tf.setDuration(new IntegerOrString(12345));
        assertEquals(Duration.ofMillis(12345), tf.javaDuration());

        tf.setDuration(new IntegerOrString("4h2m5s"));
        assertEquals(Duration.ofHours(4).plusMinutes(2).plusSeconds(5), tf.javaDuration());
    }
}