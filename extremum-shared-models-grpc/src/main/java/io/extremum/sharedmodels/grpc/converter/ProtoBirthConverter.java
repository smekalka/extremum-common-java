package io.extremum.sharedmodels.grpc.converter;

import io.extremum.sharedmodels.personal.Birth;
import io.extremum.sharedmodels.proto.common.ProtoBirth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProtoBirthConverter {
    private final ProtoZonedTimestampConverter timestampConverter;

    public Birth createFromProto(ProtoBirth proto) {
        Birth birth = new Birth();
        birth.setPlace(proto.getPlace());
        birth.setDate(timestampConverter.createFromProto(proto.getDate()));
        return birth;
    }

    public ProtoBirth createProto(Birth birth) {
        return ProtoBirth.newBuilder()
                .setPlace(birth.getPlace())
                .setDate(timestampConverter.createProto(birth.getDate()))
                .build();
    }
}
