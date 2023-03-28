package io.extremum.sharedmodels.grpc.exception;

public class ProtoConversionException extends RuntimeException {
    public ProtoConversionException(Throwable cause) {
        super(cause);
    }
}
