package io.extremum.sharedmodels.grpc.converter;

import com.google.protobuf.Int64Value;
import io.extremum.sharedmodels.dto.Pagination;
import io.extremum.sharedmodels.proto.common.ProtoPagination;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProtoPaginationConverter {
    private final ProtoZonedTimestampConverter timestampConverter;

    public ProtoPagination createProto(Pagination pagination) {
        if (pagination == null) {
            return ProtoPagination.getDefaultInstance();
        }
        ProtoPagination.Builder paginationBilder = ProtoPagination.newBuilder()
                .setOffset(pagination.getOffset())
                .setCount(pagination.getCount())
                .setSince(timestampConverter.createProto(pagination.getSince()))
                .setUntil(timestampConverter.createProto(pagination.getUntil()));
        if (pagination.getTotal() != null) {
            paginationBilder.setTotal(Int64Value.newBuilder().setValue(pagination.getTotal()).build());
        }
        return paginationBilder.build();
    }

    public Pagination createFromProto(ProtoPagination proto) {
        if (proto.equals(ProtoPagination.getDefaultInstance())) {
            return null;
        }
        return Pagination.builder()
                .offset(proto.getOffset())
                .count(proto.getCount())
                .total(proto.hasTotal() ? proto.getTotal().getValue() : null)
                .since(timestampConverter.createFromProto(proto.getSince()))
                .until(timestampConverter.createFromProto(proto.getUntil()))
                .build();
    }
}
