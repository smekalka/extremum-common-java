package io.extremum.everything.services.management;

import io.extremum.everything.services.PatchPersistenceContext;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.dto.RequestDto;

/**
 * @author rpuch
 */
final class NullPatcherHooks implements PatcherHooks {
    @Override
    public RequestDto afterPatchAppliedToDto(Model originModel, RequestDto patchedDto) {
        return patchedDto;
    }

    @Override
    public void beforeSave(PatchPersistenceContext<Model> context) {
        // doing nothing
    }

    @Override
    public void afterSave(PatchPersistenceContext<Model> context) {
        // doing nothing
    }
}
