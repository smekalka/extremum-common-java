package io.extremum.everything.services.management;

import io.extremum.everything.services.PatchPersistenceContext;
import io.extremum.everything.services.PatcherHooksService;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.dto.RequestDto;

/**
 * @author rpuch
 */
final class NonDefaultPatcherHooks implements PatcherHooks {
    private final PatcherHooksService<Model, RequestDto> patcherHooksService;

    NonDefaultPatcherHooks(PatcherHooksService<Model, RequestDto> patcherHooksService) {
        this.patcherHooksService = patcherHooksService;
    }

    @Override
    public RequestDto afterPatchAppliedToDto(Model model, RequestDto patchedDto) {
        return patcherHooksService.afterPatchAppliedToDto(model, patchedDto);
    }

    @Override
    public void beforeSave(PatchPersistenceContext<Model> context) {
        patcherHooksService.beforeSave(context);
    }

    @Override
    public void afterSave(PatchPersistenceContext<Model> context) {
        patcherHooksService.afterSave(context);
    }
}
