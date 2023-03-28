package io.extremum.everything.services.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.google.common.collect.ImmutableList;
import io.extremum.common.descriptor.service.DBDescriptorLoader;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.mapper.SystemJsonObjectMapper;
import io.extremum.common.support.CommonServices;
import io.extremum.common.support.ListBasedCommonServices;
import io.extremum.common.support.ModelClasses;
import io.extremum.everything.destroyer.PublicEmptyFieldDestroyer;
import io.extremum.everything.services.DefaultRequestDtoValidator;
import io.extremum.everything.services.GetterService;
import io.extremum.everything.services.RemovalService;
import io.extremum.everything.services.SaverService;
import io.extremum.everything.services.defaultservices.*;
import io.extremum.everything.support.DefaultModelDescriptors;
import io.extremum.everything.support.ModelDescriptors;
import io.extremum.mongo.dao.MongoCommonDao;
import io.extremum.mongo.service.impl.MongoCommonServiceImpl;
import io.extremum.security.AllowEverythingForDataAccess;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.DescriptorLoader;
import io.extremum.sharedmodels.descriptor.StaticDescriptorLoaderAccessor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.test.core.MockedMapperDependencies;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.extremum.test.mockito.ReturnFirstArg.returnFirstArg;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EverythingServicesTest {
    private DefaultEverythingEverythingManagementService service;

    private final DtoConversionService dtoConversionService = new MockDtoConversionService();
    private final ObjectMapper objectMapper = new SystemJsonObjectMapper(new MockedMapperDependencies());

    @Mock
    private MongoCommonDao<MongoModelWithoutServices> commonDaoForModelWithoutServices;
    @Mock
    private DescriptorService descriptorService;

    @Spy
    private RemovalService mongoWithServicesRemovalService = new MongoWithServicesRemovalService();
    @Spy
    private SaverService<MongoModelWithServices> mongoWithServicesSaverService
            = new MongoWithServicesSaverService();
    @InjectMocks
    private DBDescriptorLoader descriptorLoader;

    private DescriptorLoader oldDescriptorLoader;

    private final Descriptor descriptor = new Descriptor("external-id");
    private final ObjectId objectId = new ObjectId();
    private final JsonPatch jsonPatch = new JsonPatch(emptyList());

    @BeforeEach
    void initDescriptorLoader() {
        //noinspection deprecation
        oldDescriptorLoader = StaticDescriptorLoaderAccessor.getDescriptorLoader();
        StaticDescriptorLoaderAccessor.setDescriptorLoader(descriptorLoader);
    }

    @BeforeEach
    void initEverythingServicesAndManagementService() {
        MongoCommonServiceImpl<MongoModelWithoutServices> commonServiceForMongoModelWithoutServices
                = new MongoCommonServiceImpl<MongoModelWithoutServices>(commonDaoForModelWithoutServices) {
        };
        CommonServices commonServices = new ListBasedCommonServices(
                ImmutableList.of(commonServiceForMongoModelWithoutServices));
        ModelClasses modelClasses = new ConstantModelClasses(ImmutableMap.of(
                MongoModelWithServices.class.getSimpleName(), MongoModelWithServices.class,
                MongoModelWithoutServices.class.getSimpleName(), MongoModelWithoutServices.class
        ));
        ModelDescriptors modelDescriptors = new DefaultModelDescriptors(modelClasses, descriptorService);

        List<GetterService<?>> getters = ImmutableList.of(new MongoWithServicesGetterService());
        List<SaverService<?>> savers = ImmutableList.of(mongoWithServicesSaverService);
        List<RemovalService> removers = ImmutableList.of(mongoWithServicesRemovalService);

        DefaultGetter defaultGetter = new DefaultGetterViaCommonServices(commonServices, modelDescriptors, modelClasses);
        DefaultSaver defaultSaver = new DefaultSaverImpl(commonServices);
        DefaultRemover defaultRemover = new DefaultRemoverImpl(commonServices, modelDescriptors);

        ModelRetriever modelRetriever = new ModelRetriever(getters, emptyList(), defaultGetter, null,  new ModelNames(null));
        ModelSaver modelSaver = new ModelSaver(savers, defaultSaver);
        Patcher patcher = new PatcherImpl(dtoConversionService,
                objectMapper, new PublicEmptyFieldDestroyer(), new DefaultRequestDtoValidator(),
                new PatcherHooksCollection(emptyList()));
        PatchFlow patchFlow = new PatchFlowImpl(modelRetriever, patcher, modelSaver,
                new AllowEverythingForDataAccess(), new PatcherHooksCollection(emptyList()));

        service = new DefaultEverythingEverythingManagementService(
                modelRetriever,
                patchFlow,
                removers,
                defaultRemover,
                dtoConversionService,
                new AllowEverythingForDataAccess(), null, null, new ModelNames(null),null);
    }

    @AfterEach
    void restoreDescriptorLoader() {
        StaticDescriptorLoaderAccessor.setDescriptorLoader(oldDescriptorLoader);
    }

    @Test
    void givenAnEntityHasGetterService_whenGetting_thenGetterServiceShouldProvideTheResult() {
        whenGetDescriptorByExternalIdThenReturnOne(MongoModelWithServices.class.getSimpleName());

        ResponseDto dto = service.get(descriptor, false);

        assertThat(dto, is(notNullValue()));
        assertThatDtoIsForModelWithServices(dto);
    }

    private void assertThatDtoIsForModelWithServices(ResponseDto dto) {
        assertThat(dto, is(instanceOf(ResponseDtoForModelWithServices.class)));
    }

    private void whenGetDescriptorByExternalIdThenReturnOne(String modelName) {
        Descriptor descriptor = buildDescriptor(modelName);
        when(descriptorService.loadByExternalId("external-id")).thenReturn(Optional.of(descriptor));
    }

    private Descriptor buildDescriptor(String modelName) {
        return Descriptor.builder()
                .externalId("external-id")
                .internalId(objectId.toString())
                .storageType(StandardStorageType.MONGO)
                .modelType(modelName)
                .build();
    }

    @Test
    void givenAnEntityHasNoGetterService_whenGetting_thenCommonServiceShouldProvideTheResult() {
        whenGetDescriptorByExternalIdThenReturnOne(MongoModelWithoutServices.class.getSimpleName());
        whenGetDescriptorByInternalIdThenReturnOne(MongoModelWithoutServices.class.getSimpleName());
        whenFindByIdViaCommonServiceThenReturnAModel();

        ResponseDto dto = service.get(descriptor, false);

        assertThat(dto, is(notNullValue()));
        assertThatDtoIsForModelWithoutServices(dto);
    }

    private void whenFindByIdViaCommonServiceThenReturnAModel() {
        when(commonDaoForModelWithoutServices.findById(any())).thenReturn(Optional.of(new MongoModelWithoutServices()));
    }

    private void whenGetDescriptorByInternalIdThenReturnOne(String modelName) {
        Descriptor descriptor = buildDescriptor(modelName);
        when(descriptorService.loadByInternalId(objectId.toString())).thenReturn(Optional.of(descriptor));
    }

    private void assertThatDtoIsForModelWithoutServices(ResponseDto dto) {
        assertThat(dto, is(instanceOf(ResponseDtoForModelWithoutServices.class)));
    }

    @Test
    void givenAnEntityHasGetterService_whenPatching_thenShouldBeGotViaGetterService() {
        whenGetDescriptorByExternalIdThenReturnOne(MongoModelWithServices.class.getSimpleName());

        ResponseDto dto = service.patch(descriptor, jsonPatch, true);

        assertThat(dto, is(notNullValue()));
        assertThatDtoIsForModelWithServices(dto);
    }

    @Test
    void givenAnEntityHasSaverService_whenPatching_thenShouldBeSavedViaSaverService() {
        whenGetDescriptorByExternalIdThenReturnOne(MongoModelWithServices.class.getSimpleName());

        service.patch(descriptor, jsonPatch, true);

        verify(mongoWithServicesSaverService).save(isA(MongoModelWithServices.class));
    }

    @Test
    void givenAnEntityHasNoPatcherService_whenPatching_thenShouldBePatchedViaCommonService() {
        whenGetDescriptorByExternalIdThenReturnOne(MongoModelWithoutServices.class.getSimpleName());
        whenGetDescriptorByInternalIdThenReturnOne(MongoModelWithoutServices.class.getSimpleName());
        whenFindByIdViaCommonServiceThenReturnAModel();
        when(commonDaoForModelWithoutServices.save(any())).then(returnFirstArg());

        ResponseDto dto = service.patch(descriptor, jsonPatch, true);

        assertThat(dto, is(notNullValue()));
        assertThatDtoIsForModelWithoutServices(dto);
        verify(commonDaoForModelWithoutServices).save(any());
    }

    @Test
    void givenAnEntityHasRemovalService_whenDeleting_thenShouldBeRemovedViaRemovalService() {
        whenGetDescriptorByExternalIdThenReturnOne(MongoModelWithServices.class.getSimpleName());

        service.remove(descriptor);

        verify(mongoWithServicesRemovalService).remove(objectId.toString());
    }

    @Test
    void givenAnEntityHasNoRemovalService_whenDeleting_thenShouldBeRemovedViaCommonService() {
        whenGetDescriptorByExternalIdThenReturnOne(MongoModelWithoutServices.class.getSimpleName());
        whenGetDescriptorByInternalIdThenReturnOne(MongoModelWithoutServices.class.getSimpleName());
        whenFindByIdViaCommonServiceThenReturnAModel();

        service.remove(descriptor);

        verify(commonDaoForModelWithoutServices).deleteByIdAndReturn(objectId);
    }

    private static class MongoWithServicesGetterService implements GetterService<MongoModelWithServices> {
        @Override
        public MongoModelWithServices get(String id) {
            return new MongoModelWithServices();
        }

        @Override
        public Page<MongoModelWithServices> getAll(Pageable pageable) {
            return new PageImpl<>(Collections.singletonList(new MongoModelWithServices()));
        }

        @Override
        public List<MongoModelWithServices> getAllByIds(List<String> ids) {
            return null;
        }

        @Override
        public String getSupportedModel() {
            return MongoModelWithServices.class.getSimpleName();
        }
    }

    private static class MongoWithServicesSaverService implements SaverService<MongoModelWithServices> {
        @Override
        public String getSupportedModel() {
            return MongoModelWithServices.class.getSimpleName();
        }

        @Override
        public MongoModelWithServices save(MongoModelWithServices model) {
            return model;
        }
    }

    private static class MongoWithServicesRemovalService implements RemovalService {
        @Override
        public void remove(String id) {
        }

        @Override
        public String getSupportedModel() {
            return MongoModelWithServices.class.getSimpleName();
        }
    }
}
