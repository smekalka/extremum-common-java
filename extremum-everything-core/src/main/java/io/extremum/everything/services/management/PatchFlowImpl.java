package io.extremum.everything.services.management;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.security.DataSecurity;
import io.extremum.everything.services.PatchPersistenceContext;
import io.extremum.sharedmodels.descriptor.Descriptor;
import com.github.fge.jsonpatch.JsonPatch;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class PatchFlowImpl implements PatchFlow {
    private final ModelRetriever modelRetriever;
    private final Patcher patcher;
    private final ModelSaver modelSaver;
    private final DataSecurity dataSecurity;
    private final PatcherHooksCollection hooksCollection;

    public PatchFlowImpl(ModelRetriever modelRetriever,
            Patcher patcher,
            ModelSaver modelSaver,
            DataSecurity dataSecurity,
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
    public Model patch(Descriptor id, JsonPatch patch) {
        Model modelToPatch = findModel(id);

        dataSecurity.checkPatchAllowed(modelToPatch);

        Model patchedModel = patcher.patch(id, modelToPatch, patch);

        Model savedModel = saveWithHooks(id, modelToPatch, patchedModel);

        log.debug("Model with id {} has been patched with patch {}", id, patch);
        return savedModel;
    }

    private Model findModel(Descriptor id) {
        return modelRetriever.retrieveModel(id);
    }

    private Model saveWithHooks(Descriptor id, Model originalModel, Model patchedModel) {
        PatchPersistenceContext<Model> context = new PatchPersistenceContext<>(originalModel, patchedModel);

        hooksCollection.beforeSave(id.getModelType(), context);

        Model savedModel = modelSaver.saveModel(context.getPatchedModel());

        context.setCurrentStateModel(savedModel);
        hooksCollection.afterSave(id.getModelType(), context);

        return context.getCurrentStateModel();
    }

}
