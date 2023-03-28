package io.extremum.common.descriptor.resolve;

import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ReactiveDescriptorResolver {
    Mono<Void> resolveExternalIds(List<Descriptor> descriptors);
}
