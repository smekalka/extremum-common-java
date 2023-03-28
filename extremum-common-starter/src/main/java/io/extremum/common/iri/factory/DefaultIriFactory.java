package io.extremum.common.iri.factory;

import io.extremum.common.iri.properties.IriProperties;
import io.extremum.sharedmodels.basic.BasicModel;
import lombok.AllArgsConstructor;

import static io.extremum.common.descriptor.factory.DescriptorSavers.EXTERNAL_ID_TEMPLATE;

@AllArgsConstructor
public class DefaultIriFactory implements IriFactory {

    private final IriProperties iriProperties;

    @Override
    public String create(BasicModel<?> nested, BasicModel<?> folder) {
        folder.getUuid().getExternalId();
        String folderIri = folder.getIri();
        return folderIri + "/" + EXTERNAL_ID_TEMPLATE;
    }

    @Override
    public String create(BasicModel<?> nested) {
        return "/" + iriProperties.getSkolemPrefix() + "/" + EXTERNAL_ID_TEMPLATE;
    }
}