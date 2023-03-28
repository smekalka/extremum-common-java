package io.extremum.watch.services;

import io.extremum.common.support.UniversalReactiveModelLoaders;
import io.extremum.security.ReactiveDataSecurity;
import io.extremum.security.ReactiveRoleSecurity;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.watch.config.conditional.ReactiveWatchConfiguration;
import io.extremum.watch.repositories.ReactiveSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@ConditionalOnBean(ReactiveWatchConfiguration.class)
public class PersistentReactiveWatchSubscriptionService implements ReactiveWatchSubscriptionService {
    private final ReactiveSubscriptionRepository subscriptionRepository;
    private final ReactiveRoleSecurity roleSecurity;
    private final ReactiveDataSecurity dataSecurity;
    private final UniversalReactiveModelLoaders universalReactiveModelLoaders;

    @Override
    public Mono<Void> subscribe(Collection<Descriptor> ids, String subscriber) {
        Mono<Void> subscribeMono = descriptorsToInternalIds(ids)
                .flatMap(internalIds -> subscriptionRepository.subscribe(internalIds, subscriber));

        return checkWatchAllowed(ids)
                .then(subscribeMono);
    }

    private Mono<Void> checkWatchAllowed(Collection<Descriptor> ids) {
        return checkRoleSecurityAllowsToWatch(ids).and(checkDataSecurityAllowsToWatch(ids));
    }

    private Mono<Void> checkDataSecurityAllowsToWatch(Collection<Descriptor> ids) {
        return Flux.fromIterable(ids)
                .flatMap(universalReactiveModelLoaders::loadByDescriptor)
                .flatMap(dataSecurity::checkWatchAllowed)
                .then();
    }

    private Mono<Void> checkRoleSecurityAllowsToWatch(Collection<Descriptor> ids) {
        return Flux.concat(ids.stream().map(roleSecurity::checkWatchAllowed).collect(Collectors.toList())).then();
    }

    private Mono<List<String>> descriptorsToInternalIds(Collection<Descriptor> ids) {
        return Flux.fromIterable(ids)
                .flatMap(Descriptor::getInternalIdReactively)
                .collectList();
    }

    @Override
    public Mono<Void> unsubscribe(Collection<Descriptor> ids, String subscriber) {
        return descriptorsToInternalIds(ids)
                .flatMap(internalIds -> subscriptionRepository.unsubscribe(internalIds, subscriber));
    }

    @Override
    public Mono<Collection<String>> findAllSubscribersBySubscription(String subscriptionId) {
        return subscriptionRepository.getAllSubscribersIdsBySubscription(subscriptionId);
    }

    @Override
    public Mono<Boolean> isFreshSubscription(String modelId, String subscriberId) {
        return subscriptionRepository.checkFreshSubscription(modelId, subscriberId);
    }
}


