package io.extremum.sharedmodels.grpc.converter;

import com.google.protobuf.Timestamp;
import io.extremum.sharedmodels.proto.common.ProtoZonedTimestamp;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class ProtoZonedTimestampConverter {

    public ProtoZonedTimestamp createProto(ZonedDateTime dateTime) {
        if (dateTime == null) {
            return ProtoZonedTimestamp.getDefaultInstance();
        }

        Instant dateTimeInstant = dateTime.toInstant();
        return ProtoZonedTimestamp.newBuilder()
                .setZoneId(dateTime.getZone().getId())
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(dateTimeInstant.getEpochSecond())
                        .setNanos(dateTimeInstant.getNano())
                        .build())
                .build();
    }

    public ZonedDateTime createFromProto(ProtoZonedTimestamp timestamp) {
        if (timestamp.equals(ProtoZonedTimestamp.getDefaultInstance())) {
            return null;
        }
        return Instant
                .ofEpochSecond(timestamp.getTimestamp().getSeconds(),
                        timestamp.getTimestamp().getNanos())
                .atZone(ZoneId.of(timestamp.getZoneId()));
    }
}
