package io.extremum.sharedmodels.grpc.converter;

import com.google.protobuf.Timestamp;
import io.extremum.sharedmodels.proto.common.ProtoZonedTimestamp;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

class ZonedTimestampConverterTest {
    private final ProtoZonedTimestampConverter timestampConverter = new ProtoZonedTimestampConverter();

    @Test
    void testSuccessful_CreateFromProto() {
        ZonedDateTime fromZoned = ZonedDateTime.now();
        Instant from = fromZoned.toInstant();

        ProtoZonedTimestamp proto = ProtoZonedTimestamp.newBuilder()
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(from.getEpochSecond())
                        .setNanos(from.getNano())
                        .build())
                .setZoneId(fromZoned.getZone().toString())
                .build();

        ZonedDateTime toZoned = timestampConverter.createFromProto(proto);
        assertThat(toZoned, equalTo(fromZoned));
    }

    @Test
    void testBroke_CreateFromProto() {
        ProtoZonedTimestamp zoned0 = ProtoZonedTimestamp.newBuilder()
                .setZoneId(ZoneId.systemDefault().toString())
                .setTimestamp(Timestamp.newBuilder()
                        .setNanos(0)
                        .setSeconds(0)
                        .build())
                .build();
        assertThat(ProtoZonedTimestamp.getDefaultInstance(), not(zoned0));

        ProtoZonedTimestamp proto = ProtoZonedTimestamp.getDefaultInstance();
        ZonedDateTime toZoned = timestampConverter.createFromProto(proto);
        assertThat(toZoned, nullValue());
    }

    @Test
    void testSuccessful_CreateProto() {
        ZonedDateTime now = ZonedDateTime.now();
        ProtoZonedTimestamp proto = timestampConverter.createProto(now);

        assertThat(proto.getZoneId(), is(now.getZone().toString()));
        assertThat(proto.getTimestamp().getSeconds(), is(now.toEpochSecond()));
        assertThat(proto.getTimestamp().getNanos(), is(now.getNano()));
    }

    @Test
    void testBroke_CreateProto() {
        ProtoZonedTimestamp proto = timestampConverter.createProto(null);
        assertThat(proto, equalTo(ProtoZonedTimestamp.getDefaultInstance()));
    }
}