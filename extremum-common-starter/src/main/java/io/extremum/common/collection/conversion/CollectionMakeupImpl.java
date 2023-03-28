package io.extremum.common.collection.conversion;

import com.google.common.collect.ImmutableList;
import io.extremum.common.collection.service.CollectionDescriptorService;
import io.extremum.common.collection.service.ReactiveCollectionDescriptorService;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.sharedmodels.fundamental.CollectionReference;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class CollectionMakeupImpl implements CollectionMakeup {
    private final CollectionDescriptorService collectionDescriptorService;
    private final ReactiveCollectionDescriptorService reactiveCollectionDescriptorService;
    private final CollectionUrls collectionUrls;
    private final List<CollectionMakeupModule> makeupModules;
    private final ReferenceCollector collector;

    public CollectionMakeupImpl(CollectionDescriptorService collectionDescriptorService,
                                ReactiveCollectionDescriptorService reactiveCollectionDescriptorService, CollectionUrls collectionUrls,
                                List<CollectionMakeupModule> makeupModules, ReferenceCollector collector) {
        this.collectionDescriptorService = collectionDescriptorService;
        this.reactiveCollectionDescriptorService = reactiveCollectionDescriptorService;
        this.collectionUrls = collectionUrls;
        this.makeupModules = ImmutableList.copyOf(makeupModules);
        this.collector = collector;
    }

    @Override
    public void applyCollectionMakeup(ResponseDto rootDto) {
        Set<CollectionReference<?>> allVisitedReferences = new HashSet<>();

        // the loop is needed because modules can fill top which can contain
        // new collection references that we need to process as well
        while (true) {
            List<ReferenceContext> collectedReferences = collectOnlyNewReferencesToApplyMakeup(
                    collector, allVisitedReferences, rootDto);
            if (collectedReferences.isEmpty()) {
                break;
            }

            for (ReferenceContext context : collectedReferences) {
                applyMakeupToCollection(context);
            }

            addVisitedReferences(collectedReferences, allVisitedReferences);
        }
    }

    private List<ReferenceContext> collectOnlyNewReferencesToApplyMakeup(ReferenceCollector collector,
                                                                         Set<CollectionReference<?>> allVisitedReferences, ResponseDto rootDto) {
        List<ReferenceContext> collectedReferences = collector.collectReferences(rootDto);
        collectedReferences.removeIf(context -> allVisitedReferences.contains(context.getReference()));
        return collectedReferences;
    }

    private void addVisitedReferences(List<ReferenceContext> collectedReferences,
                                      Set<CollectionReference<?>> allVisitedReferences) {
        List<? extends CollectionReference<?>> justReferences = collectedReferences.stream()
                .map(ReferenceContext::getReference)
                .collect(Collectors.toList());
        allVisitedReferences.addAll(justReferences);
    }

    private void applyMakeupToCollection(ReferenceContext referenceContext) {
        CollectionDescriptor newCollectionDescriptor = referenceContext.collectionDescriptor();
        Descriptor collectionDescriptorToUse = collectionDescriptorService.retrieveByCoordinatesOrCreate(
                newCollectionDescriptor);

        referenceContext.fillReferenceId(collectionDescriptorToUse.getExternalId());
        fillCollectionUrl(referenceContext.getReference(), collectionDescriptorToUse);

        for (CollectionMakeupModule module : makeupModules) {
            CollectionMakeupRequest request = referenceContext.createMakeupRequest(collectionDescriptorToUse);
            module.applyToCollection(request);
        }
    }

    private void fillCollectionUrl(CollectionReference<?> reference,
                                   Descriptor collectionDescriptor) {
        if (reference.getIri() != null) {
            return;
        }

        String collectionExternalId = collectionDescriptor.getExternalId();

        String externalUrl = collectionUrls.collectionUrl(collectionExternalId);
        reference.setIri(externalUrl);
    }

    @Override
    public Mono<Void> applyCollectionMakeupReactively(ResponseDto rootDto) {
        ReferenceCollector collector = new OwnedCollectionReferenceCollector();
        return applyCollectionMakeupReactivelyWith(collector, rootDto);
    }

    private Mono<Void> applyCollectionMakeupReactivelyWith(ReferenceCollector collector, ResponseDto rootDto) {
        Set<CollectionReference<?>> allVisitedReferences = Collections.newSetFromMap(new ConcurrentHashMap<>());
        return applyReactivelyToNonVisitedReferences(collector, allVisitedReferences, rootDto);
    }

    private Mono<Void> applyReactivelyToNonVisitedReferences(ReferenceCollector collector,
                                                             Set<CollectionReference<?>> allVisitedReferences, ResponseDto rootDto) {
        // the recursion is needed because modules can fill top which can contain
        // new collection references that we need to process as well

        List<ReferenceContext> collectedReferences = collectOnlyNewReferencesToApplyMakeup(
                collector, allVisitedReferences, rootDto);
        if (collectedReferences.isEmpty()) {
            return Mono.empty();
        }

        return applyReactivelyToCollectedReferences(collectedReferences)
                .then(Mono.defer(() -> {
                    addVisitedReferences(collectedReferences, allVisitedReferences);
                    return applyReactivelyToNonVisitedReferences(collector, allVisitedReferences, rootDto);
                }));
    }

    private Mono<Void> applyReactivelyToCollectedReferences(List<ReferenceContext> collectedReferences) {
        return Flux.fromIterable(collectedReferences)
                // using flatMap() because we don't care about the exact order in which the references will be processed
                .flatMap(this::applyMakeupToCollectionReactively)
                .then();
    }

    private Mono<Void> applyMakeupToCollectionReactively(ReferenceContext referenceContext) {
        CollectionDescriptor newCollectionDescriptor = referenceContext.collectionDescriptor();
        return reactiveCollectionDescriptorService.retrieveByCoordinatesOrCreate(newCollectionDescriptor)
                .doOnNext(collectionDescriptor -> {
                    referenceContext.fillReferenceId(collectionDescriptor.getExternalId());
                    fillCollectionUrl(referenceContext.getReference(), collectionDescriptor);
                })
                .flatMap(collectionDescriptor -> {
                    CollectionMakeupRequest request = referenceContext.createMakeupRequest(collectionDescriptor);
                    return applyModulesReactively(request);
                })
                .then();
    }

    private Mono<Void> applyModulesReactively(CollectionMakeupRequest request) {
        return Flux.fromIterable(makeupModules)
                .concatMap(module -> module.applyToCollectionReactively(request))
                .then();
    }

    @Override
    public Mono<Void> applyCollectionMakeupReactively(CollectionReference<?> reference,
                                                      CollectionDescriptor collectionDescriptor, ResponseDto rootDto) {
        ReferenceCollector collector = new ReferenceAndOwnedCollectionsReachableFromIt(reference, collectionDescriptor);
        return applyCollectionMakeupReactivelyWith(collector, rootDto);
    }

}
