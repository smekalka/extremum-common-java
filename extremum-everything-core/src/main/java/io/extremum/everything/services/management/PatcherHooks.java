package io.extremum.everything.services.management;

import io.extremum.everything.services.PatchPersistenceContext;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.dto.RequestDto;

/**
 * @author rpuch
 */
public interface PatcherHooks {
    RequestDto afterPatchAppliedToDto(Model originModel, RequestDto patchedDto);

    void beforeSave(PatchPersistenceContext<Model> context);

    void afterSave(PatchPersistenceContext<Model> context);
}
