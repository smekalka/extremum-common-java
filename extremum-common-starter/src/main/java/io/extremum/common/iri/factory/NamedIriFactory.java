package io.extremum.common.iri.factory;

import io.extremum.common.iri.properties.IriProperties;
import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.sharedmodels.basic.Named;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import static io.extremum.common.descriptor.factory.DescriptorSavers.EXTERNAL_ID_TEMPLATE;

@AllArgsConstructor
public class NamedIriFactory implements IriFactory {

    private final IriProperties iriProperties;

    @Override
    @SneakyThrows
    public String create(BasicModel<?> nested, BasicModel<?> folder) {
        return composeIri(nested, folder);
    }

    /**
     * Compose iri for nested model.
     *
     * @param folder
     * @param nested
     * @return
     */
    @SneakyThrows
    private String composeIri(BasicModel<?> nested, BasicModel<?> folder) {
        folder.getUuid().getExternalId();
        String nestedIri;
        String folderIri = folder.getIri();
        String slug = ((Named) nested).getSlug();
        if (slug != null) {
            nestedIri = folderIri + "/" + slug;
        } else {
            nestedIri = folderIri + EXTERNAL_ID_TEMPLATE;
        }

        return nestedIri;
    }

    @Override
    public String create(BasicModel<?> model) {
        return composeIri(model);
    }

    private String composeIri(BasicModel<?> model) {
        String slug = ((Named) model).getSlug();
        if (slug != null) {
            return "/" + slug;
        }

        return "/" + iriProperties.getSkolemPrefix() + "/" + EXTERNAL_ID_TEMPLATE;
    }
}