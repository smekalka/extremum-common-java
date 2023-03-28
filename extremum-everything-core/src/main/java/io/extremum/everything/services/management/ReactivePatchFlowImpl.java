package io.extremum.everything.services.management;

import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.everything.services.PatchPersistenceContext;
import io.extremum.security.ReactiveDataSecurity;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
public class ReactivePatchFlowImpl implements ReactivePatchFlow {
    private final ModelRetriever modelRetriever;
    private final ReactivePatcher patcher;
    private final ReactiveModelSaver modelSaver;
    private final ReactiveDataSecurity dataSecurity;
    private final PatcherHooksCollection hooksCollection;

    public ReactivePatchFlowImpl(ModelRetriever modelRetriever,
                                 ReactivePatcher patcher,
                                 ReactiveModelSaver modelSaver,
                                 ReactiveDataSecurity dataSecurity,
                                 PatcherHooksCollection hooksCollection) {
        Objects.requireNonNull(modelRetriever, "modelRetriever cannot be null");
        Objects.requireNonNull(patcher, "patcher cannot be null");
        Objects.requireNonNull(modelSaver, "modelSaver cannot be null");
        Objects.requireNonNull(dataSecurity, "dataSecurity cannot be null");
        Objects.requireNonNull(hooksCollection, "hooksCollection cannot be null");

        this.modelRetriever = modelRetriever;
        this.patcher = patcher;
        this.modelSaver = modelSaver;
        this.dataSecurity = dataSecurity;
        this.hooksCollection = hooksCollection;
    }

    @Override
    public Mono<Model> patch(Descriptor id, JsonPatch patch) {
        return id.getInternalIdReactively().flatMap(internalId ->
                findModel(id)
                        .doOnNext(dataSecurity::checkPatchAllowed)
                        .flatMap(modelToPatch -> patcher.patch(id, modelToPatch, patch)
                                .flatMap(patchedModel -> saveWithHooks(id, modelToPatch, patchedModel)))
                        .contextWrite(context -> context.put(MODEL_BEING_PATCHED, internalId))
                        .doOnNext(savedModel -> log.debug("Model with id {} has been patched with patch {}", id, patch)));
    }

    private Mono<Model> findModel(Descriptor id) {
        return modelRetriever.retrieveModelReactively(id);
    }

    private Mono<Model> saveWithHooks(Descriptor id, Model originalModel, Model patchedModel) {
        return Mono.fromCallable(() -> new PatchPersistenceContext<>(originalModel, patchedModel))
                .flatMap(context -> {
                    hooksCollection.beforeSave(id.getModelType(), context);
                    return modelSaver.saveModel(context.getPatchedModel())
                            .doOnNext(context::setCurrentStateModel)
                            .doOnNext(savedModel -> hooksCollection.afterSave(id.getModelType(), context))
                            .then(Mono.fromSupplier(context::getCurrentStateModel));
                });
    }
}
