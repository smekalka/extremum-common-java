package io.extremum.sharedmodels.grpc.converter;

import com.google.protobuf.Timestamp;
import io.extremum.sharedmodels.dto.Alert;
import io.extremum.sharedmodels.proto.common.ProtoAlert;
import io.extremum.sharedmodels.proto.common.ProtoZonedTimestamp;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlertConverterTest {
    private ProtoZonedTimestampConverter zonedTimestampConverter = new ProtoZonedTimestampConverter();
    private ProtoAlertConverter alertConverter = new ProtoAlertConverter(zonedTimestampConverter);

    @Test
    void testWithoutNulls_createFromProto() {
        ZonedDateTime fromZoned = ZonedDateTime.now();
        Instant from = fromZoned.toInstant();

        ProtoAlert proto = ProtoAlert.newBuilder()
                .setTimestamp(createProtoTimestamp(fromZoned, from))
                .setCode("500")
                .setElement("element")
                .setLevel(ProtoAlert.ProtoAlertLevelEnum.ERROR)
                .setLink("link")
                .setTraceId("trace id")
                .setMessage("message")
                .build();

        Alert alert = alertConverter.createFromProto(proto);

        assertAll(
                () -> assertNotNull(alert.getTimestamp()),
                () -> assertNotNull(alert.getCode()),
                () -> assertNotNull(alert.getElement()),
                () -> assertNotNull(alert.getLevel()),
                () -> assertNotNull(alert.getLink()),
                () -> assertNotNull(alert.getTraceId()),
                () -> assertNotNull(alert.getMessage())
        );
    }

    @Test
    void testWithoutNulls_createProto() {
        Alert alert = Alert.builder()
                .withInfoLevel()
                .withElement("element")
                .withCode("200")
                .withMessage("message")
                .withTimestamp(ZonedDateTime.now())
                .build();

        ProtoAlert protoAlert = alertConverter.createProto(alert);

        assertAll(
                () -> assertTrue(protoAlert.hasTimestamp()),
                () -> assertNotNull(protoAlert.getCode()),
                () -> assertNotNull(protoAlert.getElement()),
                () -> assertEquals(protoAlert.getLevel(), ProtoAlert.ProtoAlertLevelEnum.INFO),
                () -> assertNotNull(protoAlert.getLink()),
                () -> assertNotNull(protoAlert.getTraceId()),
                () -> assertNotNull(protoAlert.getMessage())
        );
    }

    @Test
    void testWithNulls_createProto() {
        Alert alert = Alert.builder()
                .withElement(null)
                .withCode(null)
                .withMessage(null)
                .withTimestamp(null)
                .build();

        ProtoAlert proto = alertConverter.createProto(alert);

        assertAll(
                () -> assertFalse(proto.hasTimestamp(), "timestamp can't be null"),
                () -> assertNotNull(proto.getCode(), "code can't be null"),
                () -> assertNotNull(proto.getElement(), "element can't be null"),
                () -> assertEquals(proto.getLevel(), ProtoAlert.ProtoAlertLevelEnum.UNKNOWN, "level can't be null"),
                () -> assertNotNull(proto.getLink(), "link can't be null"),
                () -> assertNotNull(proto.getTraceId(), "traceId can't be null"),
                () -> assertNotNull(proto.getMessage(), "message can't be null")
        );
    }

    private ProtoZonedTimestamp createProtoTimestamp(ZonedDateTime fromZoned, Instant from) {
        return ProtoZonedTimestamp.newBuilder()
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(from.getEpochSecond())
                        .setNanos(from.getNano())
                        .build())
                .setZoneId(fromZoned.getZone().toString())
                .build();
    }
}
