//package io.extremum.common.descriptor.factory.impl;
//
//import io.extremum.descriptors.reactive.dao.ReactiveDescriptorDao;
//import io.extremum.sharedmodels.descriptor.Descriptor;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//import java.util.Collection;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class InMemoryReactiveDescriptorDao implements ReactiveDescriptorDao {
//    private final Map<String, Descriptor> descriptorMap = new ConcurrentHashMap<>();
//
//    @Override
//    public Mono<Descriptor> retrieveByExternalId(String externalId) {
//        return Mono.justOrEmpty(descriptorMap.get(externalId));
//    }
//
//    @Override
//    public Mono<Descriptor> retrieveByInternalId(String internalId) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public Mono<Descriptor> retrieveByCollectionCoordinates(String collectionCoordinates) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public Mono<Map<String, String>> retrieveMapByInternalIds(Collection<String> internalIds) {
//        throw new UnsupportedOperationException("Not implemented yet");
//    }
//
//    @Override
//    public Mono<Descriptor> retrieveByIri(String iri) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public Flux<Descriptor> retrieveByIriRegex(String pattern) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public Mono<Descriptor> store(Descriptor descriptor) {
//        descriptorMap.put(descriptor.getExternalId(), descriptor);
//        return Mono.just(descriptor);
//    }
//
//    @Override
//    public Mono<Void> destroy(String externalId) {
//        throw new UnsupportedOperationException("Not implemented yet");
//    }
//}
