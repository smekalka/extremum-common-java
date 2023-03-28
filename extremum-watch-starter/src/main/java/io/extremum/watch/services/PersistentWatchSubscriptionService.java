package io.extremum.watch.services;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.common.support.UniversalModelFinder;
import io.extremum.security.DataSecurity;
import io.extremum.security.RoleSecurity;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.watch.config.conditional.BlockingWatchConfiguration;
import io.extremum.watch.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@ConditionalOnBean(BlockingWatchConfiguration.class)
public class PersistentWatchSubscriptionService implements WatchSubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final RoleSecurity roleSecurity;
    private final DataSecurity dataSecurity;
    private final UniversalModelFinder universalModelFinder;

    @Override
    public void subscribe(Collection<Descriptor> ids, String subscriber) {
        checkWatchAllowed(ids);
        subscriptionRepository.subscribe(descriptorsToInternalIds(ids), subscriber);
    }

    private void checkWatchAllowed(Collection<Descriptor> ids) {
        checkRoleSecurityAllowsToWatch(ids);
        checkDataSecurityAllowsToWatch(ids);
    }

    private void checkDataSecurityAllowsToWatch(Collection<Descriptor> ids) {
        List<Descriptor> idsList = new ArrayList<>(ids);
        List<Model> models = universalModelFinder.findModels(idsList);
        models.forEach(dataSecurity::checkWatchAllowed);
    }

    private void checkRoleSecurityAllowsToWatch(Collection<Descriptor> ids) {
        ids.forEach(roleSecurity::checkWatchAllowed);
    }

    private List<String> descriptorsToInternalIds(Collection<Descriptor> ids) {
        return ids.stream()
                    .map(Descriptor::getInternalId)
                    .collect(Collectors.toList());
    }

    @Override
    public void unsubscribe(Collection<Descriptor> ids, String subscriber) {
        subscriptionRepository.unsubscribe(descriptorsToInternalIds(ids), subscriber);
    }

    @Override
    public Collection<String> findAllSubscribersBySubscription(String subscriptionId) {
        return subscriptionRepository.getAllSubscribersIdsBySubscription(subscriptionId);
    }

    @Override
    public Boolean isFreshSubscription(String modelId, String subscriberId) {
        return subscriptionRepository.checkFreshSubscription(modelId, subscriberId);
    }
}

