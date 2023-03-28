package io.extremum.common.iri.service;

import io.extremum.common.iri.factory.DefaultIriFactory;
import io.extremum.common.iri.factory.IriFactory;
import io.extremum.common.iri.factory.NamedIriFactory;
import io.extremum.common.iri.properties.IriProperties;
import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.sharedmodels.basic.Named;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultIriFacilitiesTest {
    DefaultIriFacilities defaultIriFacilities = new DefaultIriFacilities(new IriProperties());

    @Test
    public void Should_register_iriFactory_properly() {
        TestIriFactory testIriFactory = new TestIriFactory();
        defaultIriFacilities.register(TestModel.class, testIriFactory);

        assertEquals(testIriFactory, defaultIriFacilities.getIriFactory(TestModel.class));
    }

    @Test
    public void Should_throw_exception_if_found_more_than_two_factories() {
        TestIriFactory testIriFactory = new TestIriFactory();
        defaultIriFacilities.register(TestModel.class, testIriFactory);
        defaultIriFacilities.register(Named.class, new NamedIriFactory(new IriProperties()));

        assertThrows(
                IllegalStateException.class,
                () -> defaultIriFacilities.getIriFactory(TestModel.class),
                "Found more than 1 available IriFactory for class: io.extremum.common.iri.service.DefaultIriFacilitiesTest.TestModel"
        );
    }

    @Test
    public void Should_return_iri_factory_properly() {
        TestIriFactory testIriFactory = new TestIriFactory();
        defaultIriFacilities.register(TestModel.class, testIriFactory);

        assertEquals(testIriFactory, defaultIriFacilities.getIriFactory(TestModel.class));
    }

    @Test
    public void Should_return_default_iri_factory_if_iriFactory_for_model_does_not_exist() {
        DefaultIriFacilities defaultIriFacilities = new DefaultIriFacilities(new IriProperties());
        assertEquals(DefaultIriFactory.class, Objects.requireNonNull(defaultIriFacilities.getIriFactory(TestModel.class)).getClass());
    }


    private static class TestIriFactory implements IriFactory {
        @Override
        public String create(BasicModel<?> nested, BasicModel<?> folder) {
            return "iri-with-for-nested-model";
        }

        @Override
        public String create(BasicModel<?> nested) {
            return "iri";
        }
    }

    private static class TestModel implements BasicModel<String>, Named {

        @Override
        public String getId() {
            return null;
        }

        @Override
        public void setId(String s) {
        }

        @Override
        public Descriptor getUuid() {
            return null;
        }

        @Override
        public void setUuid(Descriptor uuid) {
        }

        @Override
        public String getIri() {
            return null;
        }

        @Override
        public void setIri(String iri) {
        }

        @Override
        public String getSlug() {
            return null;
        }

        @Override
        public void setSlug(String slug) {
        }

        @Override
        public StringOrMultilingual getName() {
            return null;
        }

        @Override
        public void setName(StringOrMultilingual name) {
        }

        @Override
        public StringOrMultilingual getDescription() {
            return null;
        }

        @Override
        public void setDescription(StringOrMultilingual description) {

        }
    }
}