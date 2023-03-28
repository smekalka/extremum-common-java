package io.extremum.sharedmodels.grpc.converter;

import com.google.protobuf.Int32Value;
import com.google.protobuf.Timestamp;
import io.extremum.sharedmodels.dto.ProjectionDto;
import io.extremum.sharedmodels.proto.common.ProtoProjection;
import io.extremum.sharedmodels.proto.common.ProtoZonedTimestamp;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectionConverterTest {
    private ProtoZonedTimestampConverter zonedTimestampConverter = new ProtoZonedTimestampConverter();
    private ProtoProjectionConverter projectionConverter = new ProtoProjectionConverter(zonedTimestampConverter);

    @Test
    void testWithoutNulls_createFromProto() {
        ZonedDateTime fromZoned = ZonedDateTime.now();
        Instant from = fromZoned.toInstant();

        ProtoProjection proto = ProtoProjection.newBuilder()
                .setLimit(Int32Value.newBuilder().setValue(100).build())
                .setOffset(Int32Value.newBuilder().setValue(100).build())
                .setUntil(createProtoTimestamp(fromZoned, from))
                .setSince(createProtoTimestamp(fromZoned, from))
                .build();

        ProjectionDto fromProto = projectionConverter.createFromProto(proto);

        assertAll(
                () -> assertNotNull(fromProto.getLimit()),
                () -> assertNotNull(fromProto.getOffset()),
                () -> assertNotNull(fromProto.getSince()),
                () -> assertNotNull(fromProto.getUntil())
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

    @Test
    void testWithoutNulls_createProto() {
        ProjectionDto projectionDto = new ProjectionDto();
        projectionDto.setUntil(ZonedDateTime.now());
        projectionDto.setSince(ZonedDateTime.now());
        projectionDto.setOffset(100);
        projectionDto.setLimit(100);

        ProtoProjection proto = projectionConverter.createProto(projectionDto);

        assertAll(
                () -> assertTrue(proto.hasSince()),
                () -> assertTrue(proto.hasUntil()),
                () -> assertTrue(proto.hasLimit()),
                () -> assertTrue(proto.hasOffset())
        );
    }

    @Test
    void testWithNulls_createProto() {
        ProjectionDto projectionDto = new ProjectionDto();
        projectionDto.setUntil(null);
        projectionDto.setSince(null);
        projectionDto.setOffset(null);
        projectionDto.setLimit(null);

        ProtoProjection proto = projectionConverter.createProto(projectionDto);

        assertAll(
                () -> assertFalse(proto.hasSince()),
                () -> assertFalse(proto.hasUntil()),
                () -> assertFalse(proto.hasLimit()),
                () -> assertFalse(proto.hasOffset())
        );
    }
}
