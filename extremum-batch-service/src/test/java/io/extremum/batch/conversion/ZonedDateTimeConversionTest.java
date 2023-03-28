package io.extremum.batch.conversion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.batch.config.ConversionConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest(classes = ConversionConfiguration.class)
public class ZonedDateTimeConversionTest {
    @Autowired
    private ObjectMapper objectMapper;
    private String stringZDT = "\"1000-01-01T10:10:10.001000+0000\"";


    @Test
    void serializeZonedDateTime() throws JsonProcessingException {
        ZonedDateTime dateTime = ZonedDateTime.of(1000, 1, 1, 10, 10, 10, 1000000, ZoneId.of("UTC"));
        String serializedZDT = objectMapper.writeValueAsString(dateTime);
        assertThat(serializedZDT, is(stringZDT));
    }

    @Test
    void deserializeZonedDateTime() throws IOException {
        ZonedDateTime dateTime = objectMapper.readValue(stringZDT, ZonedDateTime.class);
        assertThat(dateTime.getYear(), is(1000));
        assertThat(dateTime.getMonth(), is(Month.JANUARY));
        assertThat(dateTime.getDayOfMonth(), is(1));
        assertThat(dateTime.getHour(), is(10));
        assertThat(dateTime.getMinute(), is(10));
        assertThat(dateTime.getSecond(), is(10));
        assertThat(dateTime.getZone(), equalTo(ZoneId.of("Z")));
    }
}
