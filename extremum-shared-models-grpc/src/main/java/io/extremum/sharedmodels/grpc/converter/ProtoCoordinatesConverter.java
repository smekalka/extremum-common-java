package io.extremum.sharedmodels.grpc.converter;

import com.google.protobuf.DoubleValue;
import io.extremum.sharedmodels.proto.common.ProtoCoordinates;
import io.extremum.sharedmodels.spacetime.Coordinates;
import org.springframework.stereotype.Service;

@Service
public class ProtoCoordinatesConverter {

    public Coordinates createFromProto(ProtoCoordinates proto) {
        Coordinates coordinates = new Coordinates();
        if (proto.hasLatitude()) {
            coordinates.setLatitude(proto.getLatitude().getValue());
        } else {
            coordinates.setLatitude(null);
        }
        if (proto.hasLongitude()) {
            coordinates.setLongitude(proto.getLongitude().getValue());
        } else {
            coordinates.setLongitude(null);
        }
        return coordinates;
    }

    public ProtoCoordinates createProto(Coordinates coordinates) {
        return ProtoCoordinates.newBuilder()
                .setLatitude(coordinates.getLatitude() == null ? null : DoubleValue.newBuilder().setValue(coordinates.getLatitude()).build())
                .setLongitude(coordinates.getLongitude() == null ? null : DoubleValue.newBuilder().setValue(coordinates.getLongitude()).build())
                .build();
    }
}
