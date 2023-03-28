package io.extremum.common.descriptor.resolve;

import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class BatchReactiveDescriptorResolver implements ReactiveDescriptorResolver {
    private final ReactiveDescriptorService descriptorService;

    @Override
    public Mono<Void> resolveExternalIds(List<Descriptor> descriptors) {
        return Mono.defer(() -> {
            List<Descriptor> unresolvedDescriptors = descriptors.stream()
                    .filter(this::doesNotHaveExternalId)
                    .filter(Descriptor::hasInternalId)
                    .collect(Collectors.toList());
            List<String> unresolvedDescriptorInternalIds = unresolvedDescriptors.stream()
                    .map(Descriptor::getInternalId)
                    .collect(Collectors.toList());
            if (unresolvedDescriptorInternalIds.isEmpty()) {
                return Mono.empty();
            }

            return descriptorService.loadMapByInternalIds(unresolvedDescriptorInternalIds)
                    .flatMap(internalIdToExternalId -> {
                        return Mono.fromRunnable(() -> fillExternalIds(unresolvedDescriptors, internalIdToExternalId));
                    });
        });
    }

    private boolean doesNotHaveExternalId(Descriptor descriptor) {
        return !descriptor.hasExternalId();
    }

    private void fillExternalIds(List<Descriptor> unresolvedDescriptors, Map<String, String> internalIdToExternalId) {
        for (Descriptor descriptor : unresolvedDescriptors) {
            String externalId = internalIdToExternalId.get(descriptor.getInternalId());
            descriptor.setExternalId(externalId);
        }
    }
}
