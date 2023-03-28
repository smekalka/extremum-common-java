package io.extremum.common.model.advice;

import io.extremum.common.iri.service.IriFacilities;
import io.extremum.sharedmodels.basic.BasicModel;
import jodd.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;

@Slf4j
@Aspect
@RequiredArgsConstructor
public class BasicModelComposingIriAspect extends ModelMakeUpAspect<BasicModel<?>> {

    private final IriFacilities iriFacilities;

    @Override
    protected void applyToModel(BasicModel<?> model) {
        if (model.getUuid() != null) {
            model.getUuid().getExternalId();
        }
        if (model.getUuid() != null && model.getUuid().getIri() != null) {
            String iri = iriFacilities.getIriFactory(model.getClass()).create(model);
            String[] split = model.getUuid().getIri().split("/");
            split[split.length - 1] = iri.split("/")[1];
            model.setIri(StringUtil.join(split, "/"));
        } else {
            String iri = iriFacilities.getIriFactory(model.getClass()).create(model);
            model.setIri(iri);
        }
    }

    @Override
    protected void applyToModel(BasicModel<?> nested, BasicModel<?> folder) {
        String iri = iriFacilities.getIriFactory(nested.getClass()).create(nested, folder);
        nested.setIri(iri);
    }
}