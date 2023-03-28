package io.extremum.jpa.everything;

import com.google.common.collect.ImmutableList;
import io.extremum.common.collection.conversion.OwnedCollection;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.annotation.ModelName;
import io.extremum.common.reactive.NaiveReactifier;
import io.extremum.common.tx.TransactorsCollectionTransactivity;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.dao.UniversalDao;
import io.extremum.everything.services.OwnedCollectionFetcher;
import io.extremum.everything.services.GetterService;
import io.extremum.everything.services.collection.DefaultEverythingCollectionService;
import io.extremum.everything.services.collection.ListBasedCollectionProviders;
import io.extremum.everything.services.management.ModelNames;
import io.extremum.everything.services.management.ModelRetriever;
import io.extremum.jpa.model.PostgresBasicModel;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.Getter;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class DefaultCollectionFetcherManagementServiceForBasicModelEverythingCollectionFetchingTest {
    @InjectMocks
    private DefaultEverythingCollectionService service;

    @Spy
    private GetterService<JpaBasicContainer> jpaBasicContainerGetterService = new JpaBasicContainerGetter();
    @Mock
    private UniversalDao universalDao;
    @Mock
    private DtoConversionService dtoConversionService;

    @BeforeEach
    void setUp() {
        service = new DefaultEverythingCollectionService(
                new ModelRetriever(ImmutableList.of(jpaBasicContainerGetterService), emptyList(), null, null, new ModelNames(null)),
                new ListBasedCollectionProviders(
                    ImmutableList.of(new ExplicitJpaBasicElementFetcher()),
                    emptyList(), emptyList(), emptyList()
                ),
                dtoConversionService,
                universalDao, new NaiveReactifier(), new TransactorsCollectionTransactivity(emptyList()),null
        );
    }

    private void convertToResponseDtoWhenRequested() {
        when(dtoConversionService.convertUnknownToResponseDto(any(), any()))
                .thenReturn(mock(ResponseDto.class));
    }

    @Test
    void givenAJpaBasicContainerExists_whenACollectionIsFetched_thenItShouldBeReturned() {
        when(jpaBasicContainerGetterService.get("internalHostId")).thenReturn(new JpaBasicContainer());

        Descriptor hostId = jpaBasicContainerDescriptor();
        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forOwned(hostId, "elements");
        Projection projection = Projection.empty();

        CollectionFragment<ResponseDto> dtos = service.fetchCollection(collectionDescriptor, projection, false);

        assertThat(dtos.elements(), hasSize(2));
    }

    private Descriptor jpaBasicContainerDescriptor() {
        return Descriptor.builder()
                .externalId("hostId")
                .internalId("internalHostId")
                .modelType("JpaBasicContainer")
                .storageType(StandardStorageType.POSTGRES)
                .build();
    }

    @Test
    void givenAnExplicitJpaBasicCollectionFetcherIsDefined_whenCollectionIsFetched_thenItShouldBeProvidedByTheFetcher() {
        convertToResponseDtoWhenRequested();

        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forOwned(jpaBasicContainerDescriptor(),
                "explicitElements");
        Projection projection = Projection.empty();

        CollectionFragment<ResponseDto> elements = service.fetchCollection(collectionDescriptor,
                projection, false);

        assertThat(elements.elements(), hasSize(1));
    }

    @ModelName("JpaBasicElement")
    private static class JpaBasicElement extends PostgresBasicModel {
    }

    @SuppressWarnings("WeakerAccess")
    @ModelName("JpaBasicContainer")
    @Getter
    public static class JpaBasicContainer extends PostgresBasicModel {
        @OwnedCollection
        private List<JpaBasicElement> elements = Arrays.asList(new JpaBasicElement(), new JpaBasicElement());
        @SuppressWarnings("unused")
        @OwnedCollection
        private List<JpaBasicElement> explicitElements;
    }

    private static class JpaBasicContainerGetter implements GetterService<JpaBasicContainer> {
        @Override
        public JpaBasicContainer get(String id) {
            return new JpaBasicContainer();
        }

        @Override
        public Page<JpaBasicContainer> getAll(Pageable pageable) {
            return new PageImpl<>(Collections.singletonList(new JpaBasicContainer()));
        }

        @Override
        public List<JpaBasicContainer> getAllByIds(List<String> ids) {
            return null;
        }

        @Override
        public String getSupportedModel() {
            return "JpaBasicContainer";
        }
    }

    private static class ExplicitJpaBasicElementFetcher implements OwnedCollectionFetcher<JpaBasicContainer,
                    JpaBasicElement> {

        @Override
        public String getHostAttributeName() {
            return "explicitElements";
        }

        @Override
        public CollectionFragment<JpaBasicElement> fetchCollection(JpaBasicContainer container, Projection projection) {
            return CollectionFragment.forCompleteCollection(Collections.singletonList(new JpaBasicElement()));
        }

        @Override
        public String getSupportedModel() {
            return "JpaBasicContainer";
        }
    }
}