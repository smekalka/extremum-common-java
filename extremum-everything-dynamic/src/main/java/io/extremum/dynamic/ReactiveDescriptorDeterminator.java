package io.extremum.dynamic;

import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface ReactiveDescriptorDeterminator {
    Mono<Boolean> isDynamic(Descriptor id);

    Set<String> getRegisteredModelNames();
}
