package io.extremum.sharedmodels.watch;

import io.extremum.sharedmodels.basic.Model;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
public class ModelSignal<M extends Model> {

    private Instant timestamp;
    private ModelSignalType type;
    private M model;
    private String modelType;

    public ModelSignal(ModelSignalType type, M model) {
        this.timestamp = Instant.now();
        this.type = type;
        this.model = model;
        if (model != null) {
            this.modelType = model.getClass().getSimpleName();
        }
    }

    public static class Updated<M extends Model> extends ModelSignal<M> {
        public Updated(M model) {
            super(ModelSignalType.UPDATED, model);
        }
    }

    public static class Created<M extends Model> extends ModelSignal<M> {
        public Created(M model) {
            super(ModelSignalType.CREATED, model);
        }
    }

    public static class Deleted<M extends Model> extends ModelSignal<M> {
        public Deleted(M model) {
            super(ModelSignalType.DELETED, model);
        }
    }
}
