package io.extremum.common.descriptor.resolve;

import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Arrays;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchReactiveDescriptorResolverTest {
    @InjectMocks
    private BatchReactiveDescriptorResolver resolver;

    @Mock
    private ReactiveDescriptorService descriptorService;

    @Test
    void givenSomeDescriptorsNeedExternalIdUnresolved_whenResolvingDescriptorsExternalIds_thenOnlyUnresolvedShouldBeResolved() {
        // given
        when(descriptorService.loadMapByInternalIds(singletonList("internal-id")))
                .thenReturn(Mono.just(singletonMap("internal-id", "external-id-2")));

        Descriptor withExternalId = Descriptor.builder()
                .externalId("external-id")
                .build();
        Descriptor withoutExternalId = Descriptor.builder()
                .internalId("internal-id")
                .build();

        // when
        resolver.resolveExternalIds(Arrays.asList(withExternalId, withoutExternalId)).block();

        // then
        assertThat(withoutExternalId.getExternalId(), is("external-id-2"));
    }

    @Test
    void shouldNotDoAnythingIfTheDescriptorListIsEmpty() {
        resolver.resolveExternalIds(emptyList()).block();

        verify(descriptorService, never()).loadMapByInternalIds(any());
    }
}