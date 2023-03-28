package io.extremum.everything.services.management;

import io.extremum.common.collection.service.ReactiveCollectionDescriptorExtractor;
import io.extremum.common.collection.service.ReactiveCollectionOverridesWithDescriptorExtractorList;
import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.everything.collection.Projection;
import io.extremum.sharedmodels.descriptor.*;
import io.extremum.sharedmodels.dto.Response;
import io.extremum.sharedmodels.dto.ResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactiveGetDemultiplexerOnDescriptorTest {
    @InjectMocks
    private ReactiveGetDemultiplexerOnDescriptor demultiplexer;

    @Mock
    private ReactiveEverythingManagementService everythingManagementService;
    @Mock
    private EverythingCollectionManagementService collectionManagementService;
    @Mock
    private EverythingOwnedModelManagementService ownedModelService;
    @Mock
    private ReactiveDescriptorService reactiveDescriptorService;
    @Spy
    private ReactiveCollectionDescriptorExtractor collectionDescriptorExtractor =
            new ReactiveCollectionOverridesWithDescriptorExtractorList(emptyList());

    @Mock
    private ResponseDto responseDto;

    private final Descriptor singleDescriptor = Descriptor.builder()
            .externalId("external-id")
            .type(Descriptor.Type.SINGLE)
            .build();
    private final Descriptor collectionDescriptor = Descriptor.forCollection("external-id",
            CollectionDescriptor.forOwned(new Descriptor("host-id"), "items")
    );
    private final Descriptor ownedModelDescriptor = Descriptor.forOwnedModel("external-id",
            new OwnedModelDescriptor(new OwnedModelCoordinates(new OwnedCoordinates(new Descriptor("host-id"), "ownedField")))
    );

    @Test
    void givenDescriptorOfTypeSingle_whenGetting_thenEverythingGetShouldBeMade() {
        when(reactiveDescriptorService.loadByExternalId("external-id"))
                .thenReturn(Mono.just(singleDescriptor));
        when(everythingManagementService.get(same(singleDescriptor), anyBoolean()))
                .thenReturn(Mono.just(responseDto));

        Response response = demultiplexer.get(singleDescriptor.getExternalId(), Projection.empty(), false).block();

        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(sameInstance(responseDto)));
    }

    @Test
    void givenDescriptorOfTypeCollection_whenGetting_thenCollectionShouldBeFetched() {
        when(reactiveDescriptorService.loadByExternalId("external-id"))
                .thenReturn(Mono.just(collectionDescriptor));
        Response collectionOk = Response.ok();
        when(collectionManagementService.fetchCollectionReactively(same(collectionDescriptor), any(), anyBoolean()))
                .thenReturn(Mono.just(collectionOk));

        Response response = demultiplexer.get(collectionDescriptor.getExternalId(), Projection.empty(), false).block();

        assertThat(response, is(sameInstance(collectionOk)));
    }

    @Test
    void givenDescriptorOfTypeOwned_whenGetting_thenOwnedModelShouldBeFetched() {
        when(reactiveDescriptorService.loadByExternalId("external-id"))
                .thenReturn(Mono.just(ownedModelDescriptor));
        Response ownedOk = Response.ok();
        when(ownedModelService.fetchOwnedModelReactively(same(ownedModelDescriptor), anyBoolean()))
                .thenReturn(Mono.just(ownedOk));

        Response response = demultiplexer.get(ownedModelDescriptor.getExternalId(), Projection.empty(), false).block();

        assertThat(response, is(sameInstance(ownedOk)));
    }
}