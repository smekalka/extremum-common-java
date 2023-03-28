package io.extremum.everything.services.iri;

import io.extremum.descriptors.reactive.dao.ReactiveDescriptorDao;
import io.extremum.everything.services.PatchPersistenceContext;
import io.extremum.everything.services.PatcherHooksService;
import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.basic.Named;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.RequestDto;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.query.MongoRegexCreator;

@AllArgsConstructor
public abstract class NamedPatcherHooksService implements PatcherHooksService<Model, RequestDto> {

    private final ReactiveDescriptorDao reactiveDescriptorDao;

    @Override
    public RequestDto afterPatchAppliedToDto(Model model, RequestDto dto) {
        return PatcherHooksService.super.afterPatchAppliedToDto(model, dto);
    }

    @Override
    public void beforeSave(PatchPersistenceContext<Model> context) {
        PatcherHooksService.super.beforeSave(context);
    }

    @Override
    public void afterSave(PatchPersistenceContext<Model> context) {
        Model originalModel = context.getOriginalModel();
        Named patchedModel = (Named) context.getPatchedModel();

        if (originalModel instanceof Named) {
            if (isSlugChanged((Named) originalModel, patchedModel)) {
                if (originalModel instanceof MongoCommonModel) {
                    Descriptor originalDescriptor = ((MongoCommonModel) originalModel).getUuid();
                    Descriptor newDescriptor = ((MongoCommonModel) patchedModel).getUuid();
                    originalDescriptor
                            .getExternalIdReactively()
                            .then(newDescriptor.getExternalIdReactively())
                            .doOnNext(
                                    s -> {
                                        String newIri = getNewIri(patchedModel, newDescriptor);
                                        newDescriptor.setIri(newIri);
                                        String expression = MongoRegexCreator.INSTANCE.toRegularExpression(originalDescriptor.getIri(), MongoRegexCreator.MatchMode.STARTING_WITH);
                                        reactiveDescriptorDao.retrieveByIriRegex(expression).doOnNext(
                                                nestedDescriptor -> {
                                                    nestedDescriptor.setIri(nestedDescriptor.getIri().replace(
                                                            originalDescriptor.getIri(), newDescriptor.getIri()
                                                    ));
                                                    reactiveDescriptorDao.store(nestedDescriptor).subscribe();
                                                }
                                        ).subscribe();
                                    }
                            ).subscribe();
                }
            }
        }
    }

    private String getNewIri(Named patchedModel, Descriptor newDescriptor) {
        String[] split = newDescriptor.getIri().split("/");
        split[split.length - 1] = patchedModel.getSlug();

        return String.join("/", split);
    }

    private boolean isSlugChanged(Named original, Named patchedModel) {
        return !original.getSlug().equals(patchedModel.getSlug());
    }
}