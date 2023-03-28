package io.extremum.batch.model;

import lombok.*;

@Data
@Setter(AccessLevel.NONE)
public class ValidatedRequest {
    private String id;
    private Throwable ex;
    private Object data;

    public ValidatedRequest(String id, Throwable ex) {
        this.id = id;
        this.ex = ex;
    }

    public ValidatedRequest(String id, Object data) {
        this.id = id;
        this.data = data;
    }
}
