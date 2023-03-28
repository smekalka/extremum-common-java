package io.extremum.elasticsearch.facilities;

import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.common.utils.ReflectionUtils;
import io.extremum.common.uuid.StandardUUIDGenerator;
import io.extremum.common.uuid.UUIDGenerator;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

/**
 * @author rpuch
 */
public class InMemoryReactiveDescriptorService implements ReactiveDescriptorService {
    private final UUIDGenerator uuidGenerator = new StandardUUIDGenerator();
    private final ConcurrentMap<String, Descriptor> externalIdToDescriptorMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> internalIdToExternalIdMap = new ConcurrentHashMap<>();

    @Override
    public Mono<Descriptor> store(Descriptor descriptor) {
        String externalId = ReflectionUtils.getFieldValue(descriptor, "externalId");
        if (externalId == null) {
            externalId = uuidGenerator.generateUUID();
            ReflectionUtils.setFieldValue(descriptor, "externalId", externalId);
        }

        externalIdToDescriptorMap.put(externalId, descriptor);

        String internalId = ReflectionUtils.getFieldValue(descriptor, "internalId");
        if (internalId != null) {
            internalIdToExternalIdMap.put(internalId, externalId);
        }

        return Mono.just(descriptor);
    }

    @Override
    public Mono<Descriptor> loadByExternalId(String externalId) {
        return Mono.fromSupplier(() -> externalIdToDescriptorMap.get(externalId));
    }

    @Override
    public Mono<Descriptor> loadByIri(String iri) {
         throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Descriptor> loadByInternalId(String internalId) {
        return Mono.fromSupplier(() -> internalIdToExternalIdMap.get(internalId))
                .flatMap(this::loadByExternalId);
    }

    @Override
    public Mono<Map<String, String>> loadMapByInternalIds(Collection<String> internalIds) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Mono<Descriptor> makeDescriptorReady(String descriptorExternalId, String modelType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Void> destroyDescriptor(String externalId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Stream<Descriptor> descriptors() {
        return externalIdToDescriptorMap.values().stream();
    }
}
