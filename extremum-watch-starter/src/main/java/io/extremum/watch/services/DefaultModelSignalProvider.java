package io.extremum.watch.services;

import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.everything.services.management.ModelRetriever;
import io.extremum.security.CheckerContext;
import io.extremum.security.DataSecurity;
import io.extremum.security.ExtremumAccessDeniedException;
import io.extremum.security.PrincipalSource;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.watch.ModelSignal;
import io.extremum.sharedmodels.watch.ModelSignalType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.reactivestreams.Publisher;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DefaultModelSignalProvider implements ModelSignalProvider {

    private final Map<String, ModelSubscription<?>> modelSubscriptionMap = new HashMap<>();
    private final DataSecurity dataSecurity;
    private final ModelRetriever modelRetriever;
    private final PrincipalSource principalSource;

    @Override
    public <M extends Model> Publisher<M> subscribe(String modelId, ModelSignalType type) {
        Principal principal = principalSource.getPrincipal().orElseThrow(() -> new IllegalStateException("Principal is null"));
        Model model = modelRetriever.retrieveModel(Descriptor.builder().externalId(modelId).build());
        CheckerContext checkerContext = makeCheckerContext(principal);
        dataSecurity.checkWatchAllowed(model, checkerContext);
        ModelSubscription<?> modelSubscription = modelSubscriptionMap.computeIfAbsent(principal.getName(), p -> new ModelSubscription<>(principal, type));
        modelSubscription.getIds().add(modelId);

        return (Publisher<M>) modelSubscription.getPublisher();
    }


    @Override
    public <M extends Model> Publisher<M> subscribe(Class<? extends Model> modelClass, ModelSignalType type) {
        Principal principal = principalSource.getPrincipal().orElseThrow(() -> new IllegalStateException("Principal is null"));
        ModelSubscription<?> modelSubscription = modelSubscriptionMap.computeIfAbsent(principal.getName(), p -> new ModelSubscription<>(principal, type));
        modelSubscription.setModelClass(modelClass);

        return (Publisher<M>) modelSubscription.getPublisher();
    }

    @Override
    public void publish(ModelSignal signal) {
        List<ModelSubscription<?>> subscriptions = modelSubscriptionMap.values()
                .stream()
                .filter(
                        modelSubscription -> modelSubscription.getIds().contains(((BasicModel<?>) signal.getModel()).getUuid().getExternalId()) ||
                                (modelSubscription.getModelClass() != null && modelSubscription.getModelClass().equals(signal.getModel().getClass()))
                )
                .filter(modelSubscription -> modelSubscription.getType() == signal.getType())
                .collect(Collectors.toList());
        for (ModelSubscription<?> modelSubscription : subscriptions) {
            CheckerContext checkerContext = makeCheckerContext(modelSubscription.getPrincipal());
            try {
                dataSecurity.checkWatchAllowed(signal.getModel(), checkerContext);
                modelSubscription.publish(signal.getModel());
            } catch (ExtremumAccessDeniedException ignored) {

            }
        }
    }

    private CheckerContext makeCheckerContext(Principal principal) {
        return new CheckerContext() {
            @Override
            public Optional<Principal> getCurrentPrincipal() {
                return Optional.of(principal);
            }

            @Override
            public boolean currentUserHasOneOf(String... roles) {
                throw new NotImplementedException();
            }
        };
    }
}
