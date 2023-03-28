package io.extremum.everything.services;

import io.extremum.sharedmodels.basic.Model;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class PatchPersistenceContext<M extends Model> {
    /**
     * Found by ID model. Before patching
     */
    private final M originalModel;
    private final M patchedModel;

    private M currentStateModel;

    public PatchPersistenceContext(M originalModel, M patchedModel) {
        this.originalModel = originalModel;
        this.patchedModel = patchedModel;

        currentStateModel = originalModel;
    }
}
