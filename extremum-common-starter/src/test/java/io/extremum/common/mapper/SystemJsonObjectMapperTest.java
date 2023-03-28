package io.extremum.common.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.extremum.mapper.jackson.BasicJsonObjectMapper;
import io.extremum.sharedmodels.basic.IntegerOrString;
import io.extremum.sharedmodels.content.Display;
import io.extremum.sharedmodels.content.MediaType;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import io.extremum.sharedmodels.dto.Alert;
import io.extremum.sharedmodels.dto.AlertLevelEnum;
import io.extremum.sharedmodels.dto.Pagination;
import io.extremum.sharedmodels.spacetime.TimeFrame;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author rpuch
 */
class SystemJsonObjectMapperTest {
    private final MockedMapperDependencies mapperDependencies = new MockedMapperDependencies();
    private final SystemJsonObjectMapper mapper = new SystemJsonObjectMapper(mapperDependencies);

    @Test
    void testZonedDateTimeParseSuccessfully() throws JsonProcessingException {
        String utc = mapper.writeValueAsString(ZonedDateTime.of(1000, 1, 1, 1, 1, 1, 999_888_000, ZoneId.of("UTC")));
        assertThat(utc, is("\"1000-01-01T01:01:01.999888Z\""));
    }

    @Test
    void whenDescriptorIsSerialized_thenTheResultShouldBeAStringLiteralOfExternalId() throws Exception {
        Descriptor descriptor = Descriptor.builder()
                .externalId("external-id")
                .internalId("internal-id")
                .storageType(StandardStorageType.MONGO)
                .modelType("test-model")
                .build();

        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, descriptor);

        assertThat(writer.toString(), is("\"external-id\""));
    }

    @Test
    void whenDescriptorIsDeserializedFromAString_thenDescriptorObjectShouldBeTheResult() throws Exception {
        Descriptor result = mapper.readerFor(Descriptor.class).readValue("\"external-id\"");
        assertThat(result.getExternalId(), is("external-id"));
    }

    @Test
    void givenMapperIsConfiguredWithoutDescriptorsTransfiguration_whenDeserializaingDescriptorWithDisplayAndIcon_thenItShouldBeOk()
            throws Exception {
        String json = "{\"created\":\"2019-05-24T15:54:59.958000+04:00\",\"deleted\":false,\"display\":" +
                "{\"caption\":\"aaa\",\"icon\":{\"depth\":2,\"duration\":20,\"height\":200,\"type\":\"image\",\"url\"" +
                ":\"/url/to/resource\",\"width\":100},\"splash\":{\"depth\":2,\"duration\":20,\"height\":200,\"type\":" +
                "\"image\",\"url\":\"/url/to/resource\",\"width\":100}},\"externalId\":" +
                "\"1e71af1b-16f8-4567-9660-9e4549a0203f\",\"internalId\":\"5ce7db93dde97936c6c4c302\",\"modelType\":" +
                "\"test_model\",\"modified\":\"2019-05-24T15:54:59.958000+04:00\",\"storageType\":\"mongo\",\"version\":0}";

        Descriptor descriptor = new BasicJsonObjectMapper()
                .readerFor(Descriptor.class).readValue(json);

        assertThat(descriptor.getDisplay(), is(notNullValue()));
        assertThat(descriptor.getDisplay().getType(), is(Display.Type.OBJECT));
        assertThat(descriptor.getDisplay().getIcon(), is(notNullValue()));
        assertThat(descriptor.getDisplay().getIcon().getType(), is(MediaType.IMAGE));
        assertThat(descriptor.getDisplay().getIcon().getUrl(), is("/url/to/resource"));
        assertThat(descriptor.getDisplay().getIcon().getDepth(), is(2));
        assertThat(descriptor.getDisplay().getIcon().getDuration(), is(notNullValue()));
        assertThat(descriptor.getDisplay().getIcon().getDuration().isInteger(), is(true));
        assertThat(descriptor.getDisplay().getIcon().getDuration().getIntegerValue(), is(20));
        assertThat(descriptor.getDisplay().getIcon().getWidth(), is(100));
        assertThat(descriptor.getDisplay().getIcon().getHeight(), is(200));
    }

    @Test
    void givenASerializedAlertWithAnError_whenDeserializingIt_thenItShouldBeDeserializedSuccessfully()
            throws Exception {
        Alert alert = Alert.errorAlert("Oops");
        String json = mapper.writeValueAsString(alert);

        Alert deserializedAlert = mapper.readerFor(Alert.class).readValue(json);

        assertThat(deserializedAlert.isError(), is(true));
        assertThat(deserializedAlert.getMessage(), is("Oops"));
        assertThat(deserializedAlert.getLevel(), is(AlertLevelEnum.ERROR));
    }

    @Test
    void givenASerializedPagination_whenDeserializingIt_thenItShouldBeDeserializedSuccessfully()
            throws Exception {
        ZonedDateTime since = ZonedDateTime.now().plusNanos(1000).truncatedTo(ChronoUnit.MICROS); // plust 1 microsecond to test on Java8
        ZonedDateTime until = since.plusYears(1);
        Pagination pagination = Pagination.builder()
                .count(10)
                .offset(20)
                .total(100L)
                .since(since)
                .until(until)
                .build();
        String json = mapper.writeValueAsString(pagination);

        Pagination deserializedPagination = mapper.readerFor(Pagination.class).readValue(json);

        assertThat(deserializedPagination.getCount(), is(10));
        assertThat(deserializedPagination.getOffset(), is(20));
        assertThat(deserializedPagination.getTotal(), is(100L));
        assertThat(deserializedPagination.getSince().toInstant(), is(since.toInstant()));
        assertThat(deserializedPagination.getUntil().toInstant(), is(until.toInstant()));
    }

    @Test
    void givenASerializedPaginationWithoutTotal_whenDeserializingIt_thenItShouldBeDeserializedSuccessfully()
            throws Exception {
        ZonedDateTime since = ZonedDateTime.now();
        ZonedDateTime until = since.plusYears(1);
        Pagination pagination = Pagination.builder()
                .count(10)
                .offset(20)
                .since(since)
                .until(until)
                .build();
        String json = mapper.writeValueAsString(pagination);

        Pagination deserializedPagination = mapper.readerFor(Pagination.class).readValue(json);

        assertThat(deserializedPagination.getTotal(), is(nullValue()));
    }

    @Test
    void timeFrameSerializationDeserialization() throws IOException {
        TimeFrame tf = new TimeFrame();
        tf.setStart(ZonedDateTime.of(2020, 1, 2, 3, 4, 5, 6000, ZoneOffset.of("+0000")));
        tf.setEnd(ZonedDateTime.of(2020, 1, 2, 3, 4, 6, 6000, ZoneOffset.of("+0000")));
        tf.setDuration(new IntegerOrString("1s"));
        assertEquals(
                "{\"start\":\"2020-01-02T03:04:05.000006Z\",\"end\":\"2020-01-02T03:04:06.000006Z\",\"duration\":\"1s\"}",
                mapper.writeValueAsString(tf)
        );
        assertEquals(tf, mapper.readerFor(TimeFrame.class).readValue(
                "{\"start\":\"2020-01-02T03:04:05.000006+00:00\",\"end\":\"2020-01-02T03:04:06.000006+00:00\",\"duration\":\"1s\"}"
        ));
        tf.setDuration(new IntegerOrString(1000));
        assertEquals(
                "{\"start\":\"2020-01-02T03:04:05.000006Z\",\"end\":\"2020-01-02T03:04:06.000006Z\",\"duration\":1000}",
                mapper.writeValueAsString(tf)
        );
        assertEquals(tf, mapper.readerFor(TimeFrame.class).readValue(
                "{\"start\":\"2020-01-02T03:04:05.000006+00:00\",\"end\":\"2020-01-02T03:04:06.000006+00:00\",\"duration\":1000}"
        ));
        assertEquals(tf, mapper.readerFor(TimeFrame.class).readValue(
                "{\"start\":\"2020-01-02T03:04:05.000006+00:00\",\"end\":\"2020-01-02T03:04:06.000006+00:00\",\"duration\":\"1s\"}"
        ));
    }
}