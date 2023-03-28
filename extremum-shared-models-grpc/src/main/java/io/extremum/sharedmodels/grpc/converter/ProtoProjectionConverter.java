package io.extremum.sharedmodels.grpc.converter;

import com.google.protobuf.Int32Value;
import io.extremum.sharedmodels.dto.ProjectionDto;
import io.extremum.sharedmodels.proto.common.ProtoProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProtoProjectionConverter {
    private final ProtoZonedTimestampConverter timestampConverter;

    public ProtoProjection createProto(ProjectionDto projectionDto) {
        if (projectionDto == null) {
            return ProtoProjection.getDefaultInstance();
        }

        ProtoProjection.Builder protoBuilder = ProtoProjection.newBuilder();

        if (projectionDto.getSince() != null) {
            protoBuilder.setSince(timestampConverter.createProto(projectionDto.getSince()));
        }

        if (projectionDto.getUntil() != null) {
            protoBuilder.setUntil(timestampConverter.createProto(projectionDto.getUntil()));
        }

        if (projectionDto.getLimit() != null) {
            protoBuilder.setLimit(Int32Value.newBuilder().setValue(projectionDto.getLimit()).build());
        }
        if (projectionDto.getOffset() != null) {
            protoBuilder.setOffset(Int32Value.newBuilder().setValue(projectionDto.getOffset()).build());
        }
        return protoBuilder.build();
    }

    public ProjectionDto createFromProto(ProtoProjection proto) {
        if (proto == null || ProtoProjection.getDefaultInstance().equals(proto)) {
            return null;
        } else {
            ProjectionDto projectionDto = new ProjectionDto();
            projectionDto.setSince(timestampConverter.createFromProto(proto.getSince()));
            projectionDto.setUntil(timestampConverter.createFromProto(proto.getUntil()));
            if (proto.hasLimit()) {
                projectionDto.setLimit(proto.getLimit().getValue());
            }
            if (proto.hasOffset()) {
                projectionDto.setOffset(proto.getOffset().getValue());
            }
            return projectionDto;
        }
    }
}
