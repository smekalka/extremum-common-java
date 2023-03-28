package io.extremum.common.collection.conversion;

import io.extremum.common.collection.service.CollectionDescriptorService;
import io.extremum.common.collection.service.InMemoryReactiveOwnedModelDescriptorService;
import io.extremum.common.collection.service.ReactiveCollectionDescriptorService;
import io.extremum.common.collection.service.ReactiveOwnedModelDescriptorService;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.common.descriptor.factory.ReactiveDescriptorSaver;
import io.extremum.common.descriptor.factory.impl.InMemoryDescriptorService;
import io.extremum.common.descriptor.factory.impl.InMemoryReactiveDescriptorService;
import io.extremum.common.urls.TestApplicationUrls;
import io.extremum.sharedmodels.basic.IdOrObject;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.OwnedCoordinates;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.sharedmodels.fundamental.CollectionReference;
import io.extremum.sharedmodels.fundamental.CommonResponseDto;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class CollectionMakeupImplTest {
    private CollectionMakeupImpl collectionMakeup;

    @Spy
    private InMemoryDescriptorService descriptorService = new InMemoryDescriptorService();
    @Spy
    private DescriptorSaver descriptorSaver = new DescriptorSaver(descriptorService);
    @Spy
    private CollectionDescriptorService collectionDescriptorService = new InMemoryCollectionDescriptorService(
            descriptorService);
    @Spy
    private ReactiveOwnedModelDescriptorService ownedModelDescriptorService = new InMemoryReactiveOwnedModelDescriptorService();
    @Spy
    private InMemoryReactiveDescriptorService reactiveDescriptorService = new InMemoryReactiveDescriptorService();
    @Spy
    private ReactiveCollectionDescriptorService reactiveCollectionDescriptorService =
            new InMemoryReactiveCollectionDescriptorService(reactiveDescriptorService, descriptorService);
    @Spy
    private ReactiveDescriptorSaver reactiveDescriptorSaver = new ReactiveDescriptorSaver(
            descriptorService, reactiveDescriptorService, reactiveCollectionDescriptorService, ownedModelDescriptorService);
    @Spy
    private CollectionUrls collectionUrls = new CollectionUrlsInRoot(new TestApplicationUrls());

    @Mock
    private CollectionMakeupModule makeupModule;

    @Captor
    private ArgumentCaptor<CollectionMakeupRequest> makeupRequestCaptor;

    private StreetResponseDto streetDto;
    private final Descriptor descriptorInDB = Descriptor.forCollection("external-id",
            CollectionDescriptor.forOwned(
                    new Descriptor("the-street"), "the-buildings")
    );
    private OuterResponseDto outerDto;
    private OuterResponseDtoWithObjectTyping outerResponseDtoWithObjectTyping;

    @BeforeEach
    void createMakeup() {
        collectionMakeup = new CollectionMakeupImpl(collectionDescriptorService, reactiveCollectionDescriptorService,
                collectionUrls, emptyList(), new OwnedCollectionReferenceCollector());
    }

    @BeforeEach
    void initModule() {
        lenient().when(makeupModule.applyToCollectionReactively(any())).thenReturn(Mono.empty());
    }

    @BeforeEach
    void createDtos() {
        BuildingResponseDto building1 = new BuildingResponseDto("building1", "address1");
        BuildingResponseDto building2 = new BuildingResponseDto("building2", "address2");
        List<IdOrObject<Descriptor, BuildingResponseDto>> buildings = Arrays.asList(
                new IdOrObject<Descriptor, BuildingResponseDto>(building1),
                new IdOrObject<Descriptor, BuildingResponseDto>(building2)
        );
        streetDto = new StreetResponseDto("the-street", buildings);

        outerDto = new OuterResponseDto("outer-id", "inner-id", buildings);
        outerResponseDtoWithObjectTyping = new OuterResponseDtoWithObjectTyping("outer-id", "inner-id", buildings);
    }

    @Test
    void givenNoCollectionDescriptorExists_whenApplyingCollectionMakeup_thenCollectionDescriptorShouldBeFilledAndSaved() {
        collectionMakeup.applyCollectionMakeup(streetDto);

        String collectionId = streetDto.buildings.getId();
        CollectionDescriptor descriptor = retrieveNonNullSavedCollectionDescriptor(collectionId);

        assertThatStreetBuildingsCollectionGotMakeupApplied(descriptor, "the-buildings");
    }

    @NotNull
    private CollectionDescriptor retrieveNonNullSavedCollectionDescriptor(String collectionId) {
        assertThat(collectionId, is(notNullValue()));

        Descriptor descriptor = descriptorService.loadByExternalId(collectionId)
                .orElse(null);
        assertThat(descriptor, is(notNullValue()));
        assertThat(descriptor.getExternalId(), is(notNullValue()));
        assertThat(descriptor.getType(), is(Descriptor.Type.COLLECTION));

        CollectionDescriptor collectionDescriptor = descriptor.getCollection();
        assertThat(collectionDescriptor, is(notNullValue()));
        return collectionDescriptor;
    }

    private void assertThatStreetBuildingsCollectionGotMakeupApplied(CollectionDescriptor descriptor,
                                                                     String expectedHostAttributeName) {
        assertThat(descriptor, is(notNullValue()));
        assertThat(descriptor.getCoordinates(), is(notNullValue()));
        OwnedCoordinates ownedCoordinates = descriptor.getCoordinates().getOwnedCoordinates();
        assertThat(ownedCoordinates, is(notNullValue()));
        assertThat(ownedCoordinates.getHostId().getExternalId(), is("the-street"));
        assertThat(ownedCoordinates.getHostAttributeName(), is(expectedHostAttributeName));
    }

    @Test
    void givenACollectionDescriptorExists_whenApplyingCollectionMakeup_thenCollectionDescriptorShouldNotBeSaved() {
        doReturn(descriptorInDB).when(collectionDescriptorService).retrieveByCoordinatesOrCreate(any());

        collectionMakeup.applyCollectionMakeup(streetDto);

        assertThat(streetDto.buildings.getId(), is(descriptorInDB.getExternalId()));

        verify(descriptorService, never()).store(any());
    }

    @Test
    void whenApplyingCollectionMakeup_thenPrivateFieldsAreProcessedToo() {
        collectionMakeup.applyCollectionMakeup(streetDto);

        CollectionDescriptor descriptor = retrieveNonNullSavedCollectionDescriptor(streetDto.privateBuildings.getId());

        assertThatStreetBuildingsCollectionGotMakeupApplied(descriptor, "the-private-buildings");
    }

    @Test
    void givenADtoHasNullId_whenApplyCollectionMakeup_thenShouldNotChangeAnything() {
        streetDto.setId(null);

        collectionMakeup.applyCollectionMakeup(streetDto);

        assertThat(streetDto.buildings.getId(), is(nullValue()));
        verify(descriptorService, never()).store(any());
    }

    @Test
    void givenADtoHasNullCollectionReference_whenApplyCollectionMakeup_thenShouldNotChangeAnything() {
        streetDto.buildings = null;

        collectionMakeup.applyCollectionMakeup(streetDto);
    }

    @Test
    void givenHostAttributeNameIsNotSpecified_whenApplyingCollectionMakeup_thenHostAttributeNameShouldBeDeducedFromFieldName() {
        collectionMakeup.applyCollectionMakeup(streetDto);

        CollectionDescriptor descriptor = retrieveNonNullSavedCollectionDescriptor(
                streetDto.buildingsWithDefaultName.getId());

        assertThatStreetBuildingsCollectionGotMakeupApplied(descriptor, "buildingsWithDefaultName");
    }

    @Test
    void whenMakeupIsApplied_thenUrlShouldBeFilled() {
        collectionMakeup.applyCollectionMakeup(streetDto);

        String collectionId = streetDto.buildings.getId();
        assertThat(streetDto.buildings.getIri(), is("https://example.com/" + collectionId));
    }

    @Test
    void givenACollectionIsAnnotatedOnAGetter_whenMakeupIsApplied_thenIdAndUrlShouldBeFilled() {
        collectionMakeup.applyCollectionMakeup(streetDto);

        assertThat(streetDto.getBuildingsAnnotatedViaGetter().getId(), is(notNullValue()));
        String collectionId = streetDto.getBuildingsAnnotatedViaGetter().getId();
        assertThat(streetDto.getBuildingsAnnotatedViaGetter().getIri(),
                is("https://example.com/" + collectionId));
    }

    @Test
    void givenACollectionIsInsideANestedDto_whenMakeupIsApplied_thenInternalIdShouldBeSavedAsHostId() {
        collectionMakeup.applyCollectionMakeup(outerDto);

        String collectionDescriptorId = outerDto.innerDto.buildings.getId();
        CollectionDescriptor collectionDescriptor = retrieveNonNullSavedCollectionDescriptor(collectionDescriptorId);

        OwnedCoordinates coordinates = collectionDescriptor.getCoordinates().getOwnedCoordinates();
        assertThat(coordinates.getHostId().getExternalId(), is("inner-id"));
        assertThat(coordinates.getHostAttributeName(), is("the-buildings"));
    }

    @Test
    void givenACollectionIsInsideA2LevelNestedDto_whenMakeupIsApplied_thenInternalIdShouldBeSavedAsHostId() {
        ContainerResponseDto top = new ContainerResponseDto("top-id", outerDto);

        collectionMakeup.applyCollectionMakeup(top);

        String collectionDescriptorId = outerDto.innerDto.buildings.getId();
        CollectionDescriptor collectionDescriptor = retrieveNonNullSavedCollectionDescriptor(collectionDescriptorId);

        OwnedCoordinates coordinates = collectionDescriptor.getCoordinates().getOwnedCoordinates();
        assertThat(coordinates.getHostId().getExternalId(), is("inner-id"));
        assertThat(coordinates.getHostAttributeName(), is("the-buildings"));
    }

    @Test
    void givenACollectionIsInsideANestedDtoViaObjectTypedField_whenMakeupIsApplied_thenTheMakeupShouldBeApplied() {
        InnerResponseDto innerDto = (InnerResponseDto) outerResponseDtoWithObjectTyping.innerDto;

        collectionMakeup.applyCollectionMakeup(outerResponseDtoWithObjectTyping);

        String collectionDescriptorId = innerDto.buildings.getId();
        CollectionDescriptor collectionDescriptor = retrieveNonNullSavedCollectionDescriptor(collectionDescriptorId);

        OwnedCoordinates coordinates = collectionDescriptor.getCoordinates().getOwnedCoordinates();
        assertThat(coordinates.getHostId().getExternalId(), is("inner-id"));
        assertThat(coordinates.getHostAttributeName(), is("the-buildings"));
    }

    @Test
    void givenNoCollectionDescriptorExists_whenApplyingCollectionMakeupReactively_thenCollectionDescriptorShouldBeFilledAndSaved() {
        collectionMakeup.applyCollectionMakeupReactively(streetDto).block();

        String collectionId = streetDto.buildings.getId();
        CollectionDescriptor descriptor = retrieveNonNullCollectionDescriptorSavedSavedReactively(collectionId);

        assertThatStreetBuildingsCollectionGotMakeupApplied(descriptor, "the-buildings");
    }

    @NotNull
    private CollectionDescriptor retrieveNonNullCollectionDescriptorSavedSavedReactively(String collectionId) {
        assertThat(collectionId, is(notNullValue()));

        Descriptor descriptor = reactiveDescriptorService.loadByExternalId(collectionId).block();
        assertThat(descriptor, is(notNullValue()));
        assertThat(descriptor.getExternalId(), is(notNullValue()));
        assertThat(descriptor.getType(), is(Descriptor.Type.COLLECTION));

        CollectionDescriptor collectionDescriptor = descriptor.getCollection();
        assertThat(collectionDescriptor, is(notNullValue()));
        return collectionDescriptor;
    }

    @Test
    void givenACollectionDescriptorExists_whenApplyingCollectionMakeupReactively_thenCollectionDescriptorShouldNotBeSaved() {
        //noinspection UnassignedFluxMonoInstance
        doReturn(Mono.just(descriptorInDB))
                .when(reactiveCollectionDescriptorService).retrieveByCoordinatesOrCreate(any());

        collectionMakeup.applyCollectionMakeupReactively(streetDto).block();

        assertThat(streetDto.buildings.getId(), is(descriptorInDB.getExternalId()));

        //noinspection UnassignedFluxMonoInstance
        verify(reactiveDescriptorService, never()).store(any());
    }

    @Test
    void whenApplyingCollectionMakeupToCollectionReference_thenIdAndUrlShouldBeFilled() {
        CollectionReference<Object> collection = new CollectionReference<>(new ArrayList<>());
        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forFree("items");
        when(reactiveCollectionDescriptorService.retrieveByCoordinatesOrCreate(collectionDescriptor))
                .thenReturn(Mono.just(descriptorInDB));

        collectionMakeup.applyCollectionMakeupReactively(collection, collectionDescriptor, streetDto).block();

        assertThat(collection.getId(), is("external-id"));
        assertThat(collection.getIri(), is("https://example.com/external-id"));
    }

    @Test
    void whenApplyingCollectionMakeupToCollectionReference_thenIdAndUrlShouldBeFilledOnNestedCollectionReferencesAsWell() {
        CollectionReference<Object> collection = new CollectionReference<>(singletonList(streetDto));
        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forFree("items");

        collectionMakeup.applyCollectionMakeupReactively(collection, collectionDescriptor, streetDto).block();

        assertThat(streetDto.buildings.getId(), is(notNullValue()));
    }

    @Test
    void givenAModuleIsDefined_whenApplyingCollectionMakeup_thenModuleShouldBeApplied() {
        createMakeupWith1Module();

        collectionMakeup.applyCollectionMakeup(streetDto);

        verify(makeupModule, atLeast(1)).applyToCollection(any());
    }

    private void createMakeupWith1Module() {
        collectionMakeup = new CollectionMakeupImpl(collectionDescriptorService, reactiveCollectionDescriptorService,
                collectionUrls, singletonList(makeupModule), new OwnedCollectionReferenceCollector());
    }

    @Test
    void givenAModuleIsDefined_whenApplyingCollectionMakeupReactively_thenModuleShouldBeApplied() {
        createMakeupWith1Module();

        collectionMakeup.applyCollectionMakeupReactively(streetDto).block();

        //noinspection UnassignedFluxMonoInstance
        verify(makeupModule, atLeast(1)).applyToCollectionReactively(any());
    }

    @Test
    void givenAModuleIsDefined_whenApplyingCollectionMakeupToCollectionReference_thenModuleShouldBeApplied() {
        // given
        CollectionReference<Object> collection = new CollectionReference<>(new ArrayList<>());
        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forFree("items");
        when(reactiveCollectionDescriptorService.retrieveByCoordinatesOrCreate(collectionDescriptor))
                .thenReturn(Mono.just(descriptorInDB));

        createMakeupWith1Module();

        // when
        collectionMakeup.applyCollectionMakeupReactively(collection, collectionDescriptor, streetDto).block();

        // then
        //noinspection UnassignedFluxMonoInstance
        verify(makeupModule, atLeast(1)).applyToCollectionReactively(any());
    }

    @Test
    void givenAModuleIsDefinedAndItFillsTopWithMoreCollectionReferences_whenApplyingCollectionMakeup_thenAllCollectionReferencesShouldBeMadeUp() {
        // given
        createMakeupWith1Module();
        doAnswer(new FillTopWithOuterDtoOnFirstInvocationAndDoNothingThen())
                .when(makeupModule).applyToCollection(any());

        // when
        collectionMakeup.applyCollectionMakeup(streetDto);

        // then
        assertThat(outerDto.innerDto.buildings.getId(), is(notNullValue()));
    }

    @Test
    void givenAModuleIsDefinedAndItFillsTopWithMoreCollectionReferences_whenApplyingCollectionMakeupReactively_thenAllCollectionReferencesShouldBeMadeUp() {
        // given
        createMakeupWith1Module();
        //noinspection UnassignedFluxMonoInstance
        doAnswer(new FillTopWithOuterDtoOnFirstInvocationAndDoNothingThenReactively())
                .when(makeupModule).applyToCollectionReactively(any());

        // when
        collectionMakeup.applyCollectionMakeupReactively(streetDto).block();

        // then
        assertThat(outerDto.innerDto.buildings.getId(), is(notNullValue()));
    }

    private static class BuildingResponseDto extends CommonResponseDto {
        public String address;

        BuildingResponseDto(String externalId, String address) {
            setId(new Descriptor(externalId));
            this.address = address;
        }

        @Override
        public String getModel() {
            return "Building";
        }
    }

    public static class StreetResponseDto extends CommonResponseDto {
        @OwnedCollection(hostAttributeName = "the-buildings")
        public CollectionReference<IdOrObject<Descriptor, BuildingResponseDto>> buildings;
        @OwnedCollection(hostAttributeName = "the-private-buildings")
        private final CollectionReference<IdOrObject<Descriptor, BuildingResponseDto>> privateBuildings;
        @OwnedCollection
        public CollectionReference<IdOrObject<Descriptor, BuildingResponseDto>> buildingsWithDefaultName;
        private final CollectionReference<IdOrObject<Descriptor, BuildingResponseDto>> buildingsAnnotatedViaGetter;

        StreetResponseDto(String externalId,
                          List<IdOrObject<Descriptor, BuildingResponseDto>> buildings) {
            setId(new Descriptor(externalId));
            this.buildings = new CollectionReference<>(buildings);
            this.privateBuildings = new CollectionReference<>(buildings);
            this.buildingsWithDefaultName = new CollectionReference<>(buildings);
            this.buildingsAnnotatedViaGetter = new CollectionReference<>(buildings);
        }

        @OwnedCollection
        public CollectionReference<IdOrObject<Descriptor, BuildingResponseDto>> getBuildingsAnnotatedViaGetter() {
            return buildingsAnnotatedViaGetter;
        }

        @Override
        public String getModel() {
            return "Street";
        }
    }

    public static class InnerResponseDto extends CommonResponseDto {
        @OwnedCollection(hostAttributeName = "the-buildings")
        public CollectionReference<IdOrObject<Descriptor, BuildingResponseDto>> buildings;

        InnerResponseDto(String externalId,
                         List<IdOrObject<Descriptor, BuildingResponseDto>> buildings) {
            setId(new Descriptor(externalId));
            this.buildings = new CollectionReference<>(buildings);
        }

        @Override
        public String getModel() {
            return "Inner";
        }
    }

    public static class OuterResponseDto extends CommonResponseDto {
        public InnerResponseDto innerDto;

        OuterResponseDto(String outerExternalId, String innerExternalId,
                         List<IdOrObject<Descriptor, BuildingResponseDto>> buildings) {
            setId(new Descriptor(outerExternalId));
            innerDto = new InnerResponseDto(innerExternalId, buildings);
        }

        @Override
        public String getModel() {
            return "Outer";
        }
    }

    public static class OuterResponseDtoWithObjectTyping extends CommonResponseDto {
        public Object innerDto;

        OuterResponseDtoWithObjectTyping(String outerExternalId, String innerExternalId,
                                         List<IdOrObject<Descriptor, BuildingResponseDto>> buildings) {
            setId(new Descriptor(outerExternalId));
            innerDto = new InnerResponseDto(innerExternalId, buildings);
        }

        @Override
        public String getModel() {
            return "OuterWithObjectTyping";
        }
    }

    public static class ContainerResponseDto extends CommonResponseDto {
        public ResponseDto dto;

        ContainerResponseDto(String id, ResponseDto dto) {
            setId(new Descriptor(id));
            this.dto = dto;
        }

        @Override
        public String getModel() {
            return "Container";
        }
    }

    private abstract class FillTopWithOuterDtoBase implements Answer {
        private boolean first = true;

        final void fillTop(InvocationOnMock invocation) {
            if (!first) {
                return;
            }
            first = false;
            CollectionMakeupRequest request = invocation.getArgument(0);
            request.getReference().setTop((List) singletonList(outerDto));
        }
    }

    private class FillTopWithOuterDtoOnFirstInvocationAndDoNothingThen extends FillTopWithOuterDtoBase {
        @Override
        public Object answer(InvocationOnMock invocation) {
            fillTop(invocation);
            return null;
        }
    }

    private class FillTopWithOuterDtoOnFirstInvocationAndDoNothingThenReactively extends FillTopWithOuterDtoBase {
        @Override
        public Object answer(InvocationOnMock invocation) {
            return Mono.defer(() -> {
                fillTop(invocation);
                return Mono.empty();
            });
        }
    }
}