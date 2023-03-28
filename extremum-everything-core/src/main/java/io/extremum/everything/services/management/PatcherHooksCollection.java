package io.extremum.everything.services.management;

import io.extremum.common.modelservices.ModelServices;
import io.extremum.everything.services.PatchPersistenceContext;
import io.extremum.everything.services.PatcherHooksService;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.dto.RequestDto;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * @author rpuch
 */
@RequiredArgsConstructor
public class PatcherHooksCollection {
    private final List<PatcherHooksService<?, ?>> patcherHooksServices;

    public RequestDto afterPatchAppliedToDto(String modelName, Model modelToPatch, RequestDto patchedDto) {
        PatcherHooks hooks = getHooks(modelName);
        return hooks.afterPatchAppliedToDto(modelToPatch, patchedDto);
    }

    private PatcherHooks getHooks(String modelName) {
        @SuppressWarnings("unchecked")
        PatcherHooksService<Model, RequestDto> service =
                (PatcherHooksService<Model, RequestDto>) ModelServices.findServiceForModel(
                        modelName, patcherHooksServices);
        if (service != null) {
            return new NonDefaultPatcherHooks(service);
        } else {
            return new NullPatcherHooks();
        }
    }

    public void beforeSave(String modelName, PatchPersistenceContext<? extends Model> context) {
        PatcherHooks hooks = getHooks(modelName);
        hooks.beforeSave((PatchPersistenceContext<Model>) context);
    }

    public void afterSave(String modelName, PatchPersistenceContext<? extends Model> context) {
        PatcherHooks hooks = getHooks(modelName);
        hooks.afterSave((PatchPersistenceContext<Model>) context);
    }
}
