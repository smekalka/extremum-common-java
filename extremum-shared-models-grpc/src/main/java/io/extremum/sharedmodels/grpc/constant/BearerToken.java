package io.extremum.sharedmodels.grpc.constant;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;

import java.util.concurrent.Executor;

public class BearerToken extends CallCredentials {
    private final String value;

    public BearerToken(String value) {
        this.value = value;
    }

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier) {
        executor.execute(() -> {
            try {
                Metadata headers = new Metadata();
                headers.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, String.format("Bearer %s", value));
                metadataApplier.apply(headers);
            } catch (Throwable t) {
                metadataApplier.fail(Status.UNAUTHENTICATED.withCause(t));
            }
        });
    }

    @Override
    public void thisUsesUnstableApi() {
        // empty
    }
}
