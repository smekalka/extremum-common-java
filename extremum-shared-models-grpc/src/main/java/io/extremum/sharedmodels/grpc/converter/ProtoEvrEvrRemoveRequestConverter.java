package io.extremum.sharedmodels.grpc.converter;

import io.extremum.sharedmodels.proto.everything.ProtoEvrEvrRemoveRequest;
import org.springframework.stereotype.Service;

@Service
public class ProtoEvrEvrRemoveRequestConverter {

    public String extractId(ProtoEvrEvrRemoveRequest request) {
        return request.getId();
    }

    public ProtoEvrEvrRemoveRequest createProto(String id) {
        return ProtoEvrEvrRemoveRequest.newBuilder()
                .setId(id)
                .build();
    }
}
