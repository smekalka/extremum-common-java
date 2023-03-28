package io.extremum.common.descriptor.service;

import io.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class DBDescriptorLoaderTest {
    @InjectMocks
    private DBDescriptorLoader loader;

    @Mock
    private DescriptorService descriptorService;
    @Mock
    private ReactiveDescriptorService reactiveDescriptorService;

    private final Descriptor descriptor = new Descriptor("external-id");

    @Test
    void whenLoadingByExternalId_thenDescriptorShouldBeLoadedViaServiceByExternalId() {
        when(descriptorService.loadByExternalId("external-id"))
                .thenReturn(Optional.of(descriptor));

        Optional<Descriptor> result = loader.loadByExternalId("external-id");

        assertThat(result.orElse(null), is(sameInstance(descriptor)));
    }

    @Test
    void whenLoadingByInternalId_thenDescriptorShouldBeLoadedViaServiceByInternalId() {
        when(descriptorService.loadByInternalId("internal-id"))
                .thenReturn(Optional.of(descriptor));

        Optional<Descriptor> result = loader.loadByInternalId("internal-id");

        assertThat(result.orElse(null), is(sameInstance(descriptor)));
    }

    @Test
    void whenLoadingByExternalIdReactively_thenDescriptorShouldBeLoadedViaServiceByExternalIdReactively() {
        when(reactiveDescriptorService.loadByExternalId("external-id"))
                .thenReturn(Mono.just(descriptor));

        Mono<Descriptor> result = loader.loadByExternalIdReactively("external-id");

        StepVerifier.create(result)
                .assertNext(d -> assertThat(d, sameInstance(descriptor)))
                .verifyComplete();
    }

    @Test
    void whenLoadingByInternalIdReactively_thenDescriptorShouldBeLoadedViaServiceByInternalIdReactively() {
        when(reactiveDescriptorService.loadByInternalId("internal-id"))
                .thenReturn(Mono.just(descriptor));

        Mono<Descriptor> result = loader.loadByInternalIdReactively("internal-id");

        StepVerifier.create(result)
                .assertNext(d -> assertThat(d, sameInstance(descriptor)))
                .verifyComplete();
    }
}