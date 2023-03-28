package io.extremum.sharedmodels.grpc.converter;

import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.sharedmodels.proto.everything.ProtoEvrEvrPatchRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class ProtoEvrEvrPatchRequestConverter {

    public String extractId(ProtoEvrEvrPatchRequest request) {
        return request.getId();
    }

    public JsonPatch extractPatch(ProtoEvrEvrPatchRequest request) {
        try {
            return JsonPatch.fromJson(JacksonUtils.getReader().readTree(request.getJsonPatch()));
        } catch (IOException e) {
            log.error("Unable to create JsonPatch from source {}", request.getJsonPatch());
            throw new RuntimeException("Unable to create JsonPatch from source " + request.getJsonPatch(), e);
        }
    }

    public boolean extractExpand(ProtoEvrEvrPatchRequest request) {
        return request.getExpand();
    }

    public ProtoEvrEvrPatchRequest createProto(String id, String jsonPatch, boolean expand) {
        return ProtoEvrEvrPatchRequest.newBuilder()
                .setId(id)
                .setJsonPatch(jsonPatch)
                .setExpand(expand)
                .build();
    }
}
