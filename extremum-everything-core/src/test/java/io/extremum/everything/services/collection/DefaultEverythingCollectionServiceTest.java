package io.extremum.everything.services.collection;

import com.google.common.collect.ImmutableList;
import io.extremum.common.collection.conversion.OwnedCollection;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.annotation.ModelName;
import io.extremum.common.reactive.NaiveReactifier;
import io.extremum.common.reactive.Reactifier;
import io.extremum.common.tx.CollectionTransactivity;
import io.extremum.everything.collection.CollectionElementType;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.dao.UniversalDao;
import io.extremum.everything.exceptions.EverythingEverythingException;
import io.extremum.everything.services.*;
import io.extremum.everything.services.management.ModelNames;
import io.extremum.everything.services.management.ModelRetriever;
import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class DefaultEverythingCollectionServiceTest {
    @InjectMocks
    private DefaultEverythingCollectionService service;

    @Spy
    private GetterService<Street> streetGetterService = new StreetGetter();
    @Spy
    private ReactiveGetterService<Street> streetReactiveGetterService = new StreetReactiveGetter();
    @Mock
    private UniversalDao universalDao;
    @Mock
    private DtoConversionService dtoConversionService;
    @Spy
    private CollectionTransactivity transactivity = new TestCollectionTransactivity();
    @SuppressWarnings("deprecation")
    @Spy
    private Reactifier reactifier = new NaiveReactifier();
    @Spy
    private StreetFreeFetcher streetFreeFetcher = new StreetFreeFetcher();
    @Spy
    private StreetFreeStreamer streetFreeStreamer = new StreetFreeStreamer();

    private static final ObjectId id1 = new ObjectId();
    private static final ObjectId id2 = new ObjectId();

    @BeforeEach
    void setUp() {
        convertToResponseDtoWhenRequested();
        convertToResponseDtoReactivelyWhenRequested();

        service = new DefaultEverythingCollectionService(
                new ModelRetriever(ImmutableList.of(streetGetterService),
                        ImmutableList.of(streetReactiveGetterService), null, null, new ModelNames(null)),
                new ListBasedCollectionProviders(
                        singletonList(new ExplicitHouseFetcher()),
                        singletonList(new ExplicitHouseStreamer()),
                        singletonList(streetFreeFetcher),
                        singletonList(streetFreeStreamer)
                ),
                dtoConversionService,
                universalDao, reactifier, transactivity, null
        );
    }

    private void convertToResponseDtoWhenRequested() {
        lenient().when(dtoConversionService.convertUnknownToResponseDto(any(), any()))
                .thenReturn(mock(ResponseDto.class));
    }

    private void convertToResponseDtoReactivelyWhenRequested() {
        lenient().when(dtoConversionService.convertUnknownToResponseDtoReactively(any(), any()))
                .thenReturn(Mono.just(mock(ResponseDto.class)));
    }

    @Test
    void givenHostExistsAndNoCollectionFetcherRegistered_whenCollectionIsFetched_thenItShouldBeReturned() {
        returnStreetAndHousesAndConvertToDtosInBlockingMode();

        CollectionDescriptor collectionDescriptor = housesCollectionDescriptor();
        Projection projection = Projection.empty();

        CollectionFragment<ResponseDto> dtos = service.fetchCollection(collectionDescriptor, projection, false);

        assertThat(dtos.elements(), hasSize(2));
    }

    private void returnStreetWhenRequested() {
        when(streetGetterService.get("internalHostId")).thenReturn(new Street());
    }

    private void retrieve2HousesWhenRequestedByIds() {
        when(universalDao.retrieveByIds(eq(Arrays.asList(id1, id2)), eq(House.class), any()))
                .thenReturn(CollectionFragment.forCompleteCollection(Arrays.asList(new House(), new House())));
    }

    private Descriptor streetDescriptor() {
        return Descriptor.builder()
                .externalId("hostId")
                .internalId("internalHostId")
                .modelType("Street")
                .storageType(StandardStorageType.MONGO)
                .build();
    }

    @Test
    void givenHostDoesNotExist_whenCollectionIsFetched_thenAnExceptionShouldBeThrown() {
        when(streetGetterService.get("internalHostId")).thenReturn(null);

        CollectionDescriptor collectionDescriptor = housesCollectionDescriptor();
        Projection projection = Projection.empty();

        try {
            service.fetchCollection(collectionDescriptor, projection, false);
            fail("An exception should be thrown");
        } catch (EverythingEverythingException e) {
            assertThat(e.getMessage(), is("No host entity was found by external ID 'hostId'"));
        }
    }

    @Test
    void givenAnExplicitCollectionFetcherIsDefined_whenCollectionIsFetched_thenItShouldBeProvidedByTheFetcher() {
        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forOwned(streetDescriptor(),
                "explicitHouses");
        Projection projection = Projection.empty();

        CollectionFragment<ResponseDto> houses = service.fetchCollection(collectionDescriptor, projection, false);

        assertThat(houses.elements(), hasSize(1));
    }

    @Test
    void givenHostExistsAndNoCollectionStreamerRegistered_whenCollectionIsFetchedReactively_thenItShouldBeReturned() {
        returnStreetAndHousesAndConvertToDtosInReactiveMode();

        CollectionDescriptor collectionDescriptor = housesCollectionDescriptor();
        Projection projection = Projection.empty();

        Mono<CollectionFragment<ResponseDto>> resultMono = service.fetchCollectionReactively(collectionDescriptor,
                projection, false);
        CollectionFragment<ResponseDto> dtos = resultMono.block();

        assertThat(dtos, is(notNullValue()));
        assertThat(dtos.elements(), hasSize(2));
    }

    private void returnStreetAndHousesAndConvertToDtosInReactiveMode() {
        when(streetReactiveGetterService.get("internalHostId")).thenReturn(Mono.just(new Street()));
        stream2HousesWhenRequestedByIds();
    }

    @Test
    void givenHostDoesNotExist_whenCollectionIsFetchedReactively_thenAnExceptionShouldBeThrown() {
        when(streetReactiveGetterService.get("internalHostId")).thenReturn(Mono.empty());

        CollectionDescriptor collectionDescriptor = housesCollectionDescriptor();
        Projection projection = Projection.empty();

        try {
            service.fetchCollectionReactively(collectionDescriptor, projection, false).block();
            fail("An exception should be thrown");
        } catch (EverythingEverythingException e) {
            assertThat(e.getMessage(), is("No host entity was found by external ID 'hostId'"));
        }
    }

    @Test
    void givenAnExplicitCollectionStreamerIsDefined_whenCollectionIsFetchedReactively_thenItShouldBeProvidedByTheStreamer() {
        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forOwned(streetDescriptor(),
                "explicitHouses");
        Projection projection = Projection.empty();

        Mono<CollectionFragment<ResponseDto>> resultMono = service.fetchCollectionReactively(collectionDescriptor,
                projection, false);
        CollectionFragment<ResponseDto> houses = resultMono.block();

        assertThat(houses, is(notNullValue()));
        assertThat(houses.elements(), hasSize(1));
    }

    @Test
    void givenHostExistsAndNoCollectionStreamerRegistered_whenCollectionIsStreamed_thenItShouldBeReturned() {
        returnStreetReactivelyWhenRequested();
        stream2HousesWhenRequestedByIds();

        CollectionDescriptor collectionDescriptor = housesCollectionDescriptor();
        Projection projection = Projection.empty();

        Flux<ResponseDto> dtos = service.streamCollection(collectionDescriptor, projection, false);

        assertThat(dtos.toStream().collect(Collectors.toList()), hasSize(2));
    }

    private void stream2HousesWhenRequestedByIds() {
        when(universalDao.streamByIds(eq(Arrays.asList(id1, id2)), eq(House.class), any()))
                .thenReturn(Flux.just(new House(), new House()));
    }

    private void returnStreetReactivelyWhenRequested() {
        when(streetReactiveGetterService.get("internalHostId")).thenReturn(Mono.just(new Street()));
    }

    @Test
    void givenHostDoesNotExist_whenCollectionIsStreamed_thenAnExceptionShouldBeThrown() {
        when(streetReactiveGetterService.get("internalHostId")).thenReturn(Mono.empty());

        CollectionDescriptor collectionDescriptor = housesCollectionDescriptor();
        Projection projection = Projection.empty();

        AtomicReference<Throwable> throwableRef = new AtomicReference<>();

        service.streamCollection(collectionDescriptor, projection, false)
                .doOnError(throwableRef::set)
                .subscribe();

        assertThat(throwableRef.get(), is(notNullValue()));
        assertThat(throwableRef.get().getMessage(), is("No host entity was found by external ID 'hostId'"));
    }

    @Test
    void givenAnExplicitCollectionFetcherIsDefined_whenCollectionIsStreamed_thenItShouldBeProvidedByTheFetcher() {
        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forOwned(streetDescriptor(),
                "explicitHouses");
        Projection projection = Projection.empty();

        Flux<ResponseDto> houses = service.streamCollection(collectionDescriptor, projection, false);

        assertThat(houses.toStream().collect(Collectors.toList()), hasSize(1));
    }

    @Test
    void givenHostExistsAndTransactivityRequiresExecuteInATransaction_whenCollectionIsStreamed_thenItShouldStreamed() {
        when(transactivity.isCollectionTransactional(any())).thenReturn(true);
        returnStreetWhenRequested();
        retrieve2HousesWhenRequestedByIds();

        Flux<ResponseDto> dtos = service.streamCollection(housesCollectionDescriptor(), Projection.empty(), false);

        assertThat(dtos.toStream().collect(Collectors.toList()), hasSize(2));
    }

    private void returnStreetAndHousesAndConvertToDtosInBlockingMode() {
        returnStreetWhenRequested();
        retrieve2HousesWhenRequestedByIds();
    }

    @NotNull
    private CollectionDescriptor housesCollectionDescriptor() {
        Descriptor hostId = streetDescriptor();
        return CollectionDescriptor.forOwned(hostId, "houses");
    }

    @Test
    void givenHostExistsAndTransactivityRequiresExecuteInATransaction_whenCollectionIsStreamed_thenItShouldBeFetchedInsideATransactionAndReactified() {
        when(transactivity.isCollectionTransactional(any())).thenReturn(true);
        returnStreetWhenRequested();
        retrieve2HousesWhenRequestedByIds();

        Flux<ResponseDto> dtos = service.streamCollection(housesCollectionDescriptor(), Projection.empty(), false);
        dtos.blockLast();

        verify(transactivity).doInTransaction(any(), any());
        //noinspection UnassignedFluxMonoInstance
        verify(reactifier).flux(any());
    }

    @Test
    void givenDescriptorIsForFreeStreetsCollection_whenFetchingTheCollection_thenStreetsFromFreeFetcherShouldBeReturned() {
        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forFree("streets");

        CollectionFragment<ResponseDto> streets = service.fetchCollection(collectionDescriptor, Projection.empty(), false);

        assertThat(streets.total().orElse(999), is(3L));
        assertThat(streets.elements(), hasSize(3));
        verify(streetFreeFetcher).fetchCollection(isNull(), any());
    }

    @Test
    void givenDescriptorIsForFreeStreetsCollection_whenStreamingTheCollection_thenStreetsFromFreeStreamerShouldBeReturned() {
        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forFree("streets");

        List<ResponseDto> streets = service.streamCollection(collectionDescriptor, Projection.empty(), false)
                .toStream().collect(Collectors.toList());

        assertThat(streets, hasSize(3));
        //noinspection UnassignedFluxMonoInstance
        verify(streetFreeStreamer).streamCollection(isNull(), any());
    }

    @ModelName("House")
    private static class House extends MongoCommonModel {
    }

    @SuppressWarnings("WeakerAccess")
    @ModelName("Street")
    @Getter
    public static class Street extends MongoCommonModel {
        @OwnedCollection
        @CollectionElementType(House.class)
        private final List<String> houses = Arrays.asList(id1.toString(), id2.toString());
        @SuppressWarnings("unused")
        @OwnedCollection
        private List<String> explicitHouses;
    }

    private static class StreetGetter implements GetterService<Street> {
        @Override
        public Street get(String id) {
            return new Street();
        }

        @Override
        public Page<Street> getAll(Pageable pageable) {
            return new PageImpl<>(Collections.singletonList(new Street()));
        }

        @Override
        public List<Street> getAllByIds(List<String> ids) {
            return null;
        }

        @Override
        public String getSupportedModel() {
            return "Street";
        }
    }

    private static class StreetReactiveGetter implements ReactiveGetterService<Street> {
        @Override
        public Mono<Street> get(String id) {
            return Mono.just(new Street());
        }

        @Override
        public String getSupportedModel() {
            return "Street";
        }
    }

    private static class ExplicitHouseFetcher implements OwnedCollectionFetcher<Street, House> {

        @Override
        public String getHostAttributeName() {
            return "explicitHouses";
        }

        @Override
        public CollectionFragment<House> fetchCollection(Street street, Projection projection) {
            return CollectionFragment.forCompleteCollection(singletonList(new House()));
        }

        @Override
        public String getSupportedModel() {
            return "Street";
        }
    }

    private static class ExplicitHouseStreamer implements OwnedCollectionStreamer<Street, House> {

        @Override
        public String getHostAttributeName() {
            return "explicitHouses";
        }

        @Override
        public Flux<House> streamCollection(Street street, Projection projection) {
            return Flux.just(new House());
        }

        @Override
        public String getSupportedModel() {
            return "Street";
        }
    }

    private static class TestCollectionTransactivity implements CollectionTransactivity {
        @Override
        public boolean isCollectionTransactional(Descriptor hostId) {
            return false;
        }

        @Override
        public <T> T doInTransaction(Descriptor hostId, Supplier<T> action) {
            return action.get();
        }
    }

    private static class StreetFreeFetcher implements FreeCollectionFetcher<Street> {

        @Override
        public String getCollectionName() {
            return "streets";
        }

        @Override
        public CollectionFragment<Street> fetchCollection(String parametersString, Projection projection) {
            List<Street> streets = Arrays.asList(new Street(), new Street(), new Street());
            return CollectionFragment.forCompleteCollection(streets);
        }
    }

    private static class StreetFreeStreamer implements FreeCollectionStreamer<Street> {

        @Override
        public String getCollectionName() {
            return "streets";
        }

        @Override
        public Flux<Street> streamCollection(String parametersString, Projection projection) {
            return Flux.just(new Street(), new Street(), new Street());
        }
    }
}