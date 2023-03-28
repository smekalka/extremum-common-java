package io.extremum.everything.services.iri;

import io.extremum.descriptors.reactive.dao.ReactiveDescriptorDao;
import io.extremum.everything.services.PatchPersistenceContext;
import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.basic.Named;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NamedPatcherHooksServiceTest {

    @InjectMocks
    private TestPatcherHooksService testPatcherHooksService;

    @Mock
    private ReactiveDescriptorDao reactiveDescriptorDao;

    @Captor
    ArgumentCaptor<Descriptor> captor;

    @Test
    void ShouldChangeIriOfNestedModelsAfterSave() {
        TestModel originalModel = new TestModel();
        originalModel.setSlug("old-slug");
        originalModel.setUuid(Descriptor.builder().externalId("externalid").internalId("internalid").iri("/a/old-slug").build());

        TestModel patchedModel = new TestModel();
        patchedModel.setSlug("new-slug");
        patchedModel.setUuid(Descriptor.builder().externalId("externalid").internalId("internalid").iri("/a/old-slug").build());

        PatchPersistenceContext<Model> context = new PatchPersistenceContext<>(originalModel, patchedModel);

        when(reactiveDescriptorDao.retrieveByIriRegex(any())).thenReturn(
                Flux.fromIterable(Arrays.asList(
                        Descriptor.builder().iri("/a/old-slug/1").build(),
                        Descriptor.builder().iri("/a/old-slug/2").build(),
                        Descriptor.builder().iri("/a/old-slug/3").build()
                ))
        );
        when(reactiveDescriptorDao.store(any())).thenReturn(Mono.just(Descriptor.builder().build()));

        testPatcherHooksService.afterSave(context);

        verify(reactiveDescriptorDao, times(3)).store(captor.capture());

        Assertions.assertTrue(captor.getAllValues()
                .stream()
                .map(Descriptor::getIri)
                .collect(Collectors.toList())
                .containsAll(Arrays.asList("/a/new-slug/1", "/a/new-slug/2", "/a/new-slug/3")));
    }

    private static class TestModel extends MongoCommonModel implements Named {

        private StringOrMultilingual description;
        private String slug;
        private StringOrMultilingual name;

        @Override
        public StringOrMultilingual getDescription() {
            return description;
        }

        @Override
        public void setDescription(StringOrMultilingual description) {
            this.description = description;
        }

        @Override
        public String getSlug() {
            return slug;
        }

        @Override
        public void setSlug(String slug) {
            this.slug = slug;

        }

        @Override
        public StringOrMultilingual getName() {
            return name;
        }

        @Override
        public void setName(StringOrMultilingual name) {
            this.name = name;
        }
    }

    private static class TestPatcherHooksService extends NamedPatcherHooksService {

        public TestPatcherHooksService(ReactiveDescriptorDao reactiveDescriptorDao) {
            super(reactiveDescriptorDao);
        }

        @Override
        public String getSupportedModel() {
            return "Model";
        }
    }
}