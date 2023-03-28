package io.extremum.dynamic;

import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class DefaultReactiveDescriptorDeterminator implements ReactiveDescriptorDeterminator {
    private final SchemaMetaService schemaMetaService;

    @Override
    public Mono<Boolean> isDynamic(Descriptor id) {
        return id.getModelTypeReactively()
                .map(schemaMetaService.getModelNames()::contains);
    }

    @Override
    public Set<String> getRegisteredModelNames() {
        return schemaMetaService.getModelNames();
    }
}
