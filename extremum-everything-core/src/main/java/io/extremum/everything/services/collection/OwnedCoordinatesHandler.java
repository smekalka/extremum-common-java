package io.extremum.everything.services.collection;

import io.extremum.common.reactive.Reactifier;
import io.extremum.common.tx.CollectionTransactivity;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.dao.UniversalDao;
import io.extremum.everything.exceptions.EverythingEverythingException;
import io.extremum.everything.services.OwnedCollectionFetcher;
import io.extremum.everything.services.OwnedCollectionStreamer;
import io.extremum.everything.services.management.ModelRetriever;
import io.extremum.everything.services.management.ModelSaver;
import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.CollectionCoordinates;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.OwnedCoordinates;
import io.extremum.sharedmodels.dto.RequestDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.BiFunction;

import static java.lang.String.format;

@RequiredArgsConstructor
class OwnedCoordinatesHandler implements CoordinatesHandler {
    private final ModelRetriever modelRetriever;
    private final UniversalDao universalDao;
    private final CollectionProviders collectionProviders;
    private final Reactifier reactifier;
    private final CollectionTransactivity transactivity;
    private final ModelSaver modelSaver;

    @Override
    @SuppressWarnings("rawtypes")
    public CollectionFragment<Model> fetchCollection(CollectionCoordinates coordinates, Projection projection) {
        OwnedCoordinates owned = coordinates.getOwnedCoordinates();
        BasicModel host = retrieveHost(owned);

        Optional<OwnedCollectionFetcher> optFetcher = collectionProviders.findOwnedFetcher(owned);

        @SuppressWarnings("unchecked")
        CollectionFragment<Model> castResult = optFetcher
                .map(fetcher -> fetcher.fetchCollection(host, projection))
                .orElseGet(() -> fetchUsingDefaultConvention(owned, host, projection));
        return castResult;
    }

    @SuppressWarnings("rawtypes")
    private BasicModel retrieveHost(OwnedCoordinates owned) {
        Model host = modelRetriever.retrieveModel(owned.getHostId());
        if (host == null) {
            throw createHostNotFoundException(owned);
        }

        return castToBasicModel(owned, host);
    }

    private EverythingEverythingException createHostNotFoundException(OwnedCoordinates owned) {
        String message = format("No host entity was found by external ID '%s'",
                owned.getHostId().getExternalId());
        return new EverythingEverythingException(message);
    }

    @SuppressWarnings("rawtypes")
    private BasicModel castToBasicModel(OwnedCoordinates owned, Model host) {
        if (!(host instanceof BasicModel)) {
            throw new EverythingEverythingException(String.format("Host '%s' is not a BasicModel",
                    owned.getHostId().getModelType()));
        }

        return (BasicModel) host;
    }

    @SuppressWarnings("rawtypes")
    private CollectionFragment<Model> fetchUsingDefaultConvention(OwnedCoordinates owned,
                                                                  BasicModel host, Projection projection) {
        FetchByOwnedCoordinates fetcher = new FetchByOwnedCoordinates(universalDao);
        return fetcher.fetch(host, owned.getHostAttributeName(), projection);
    }

    @Override
    public Flux<Model> streamCollection(CollectionCoordinates coordinates, Projection projection) {
        OwnedCoordinates owned = coordinates.getOwnedCoordinates();
        if (dbAccessShouldBeMadeInATransaction(owned)) {
            return streamBlockingFetchResult(coordinates, projection, owned);
        }

        return streamReactively(projection, owned);
    }

    private boolean dbAccessShouldBeMadeInATransaction(OwnedCoordinates owned) {
        return transactivity.isCollectionTransactional(owned.getHostId());
    }

    private Flux<Model> streamBlockingFetchResult(CollectionCoordinates coordinates, Projection projection,
                                                  OwnedCoordinates owned) {
        return reactifier.flux(() -> transactivity.doInTransaction(owned.getHostId(), () -> fetchCollection(coordinates, projection).elements()));
    }

    @SuppressWarnings("rawtypes")
    private Flux<Model> streamReactively(Projection projection, OwnedCoordinates owned) {
        Mono<BasicModel> hostMono = retrieveHostReactively(owned);

        Optional<OwnedCollectionStreamer> optStreamer = collectionProviders.findOwnedStreamer(owned);

        if (optStreamer.isPresent()) {
            OwnedCollectionStreamer streamer = optStreamer.get();
            @SuppressWarnings("unchecked")
            Flux<Model> castModels = hostMono.flatMapMany(host -> streamer.streamCollection(host, projection));
            return castModels;
        } else {
            return fetchUsingDefaultConventionReactively(owned, hostMono, projection);
        }
    }

    @SuppressWarnings("rawtypes")
    private Mono<BasicModel> retrieveHostReactively(OwnedCoordinates coordinates) {
        return modelRetriever.retrieveModelReactively(coordinates.getHostId())
                .switchIfEmpty(Mono.error(() -> createHostNotFoundException(coordinates)))
                .map(host -> castToBasicModel(coordinates, host));
    }

    @SuppressWarnings("rawtypes")
    private Flux<Model> fetchUsingDefaultConventionReactively(OwnedCoordinates owned,
                                                              Mono<BasicModel> hostMono,
                                                              Projection projection) {
        StreamByOwnedCoordinates streamer = new StreamByOwnedCoordinates(universalDao);
        return hostMono.flatMapMany(host ->
                streamer.stream(host, owned.getHostAttributeName(), projection));
    }

    @SneakyThrows
    @Transactional
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Model addToCollection(CollectionCoordinates coordinates, BiFunction<RequestDto, Class<? extends BasicModel<?>>, BasicModel<?>> function, RequestDto requestDto) {
        OwnedCoordinates owned = coordinates.getOwnedCoordinates();
        BasicModel host = retrieveHost(owned);
        BeanWrapper configurablePropertyAccessor = PropertyAccessorFactory.forBeanPropertyAccess(host);
        Collection<BasicModel<?>> propertyValue = (Collection<BasicModel<?>>) configurablePropertyAccessor.getPropertyValue(owned.getHostAttributeName());
        Method writeMethod = configurablePropertyAccessor.getPropertyDescriptor(owned.getHostAttributeName()).getWriteMethod();

        ParameterizedType listType = (ParameterizedType) writeMethod.getGenericParameterTypes()[0];
        Class<? extends BasicModel<?>> listItemClass = (Class<? extends BasicModel<?>>) listType.getActualTypeArguments()[0];

        if (propertyValue == null) {
            throw new IllegalStateException("Can't add item to collection");
        }

        propertyValue.add(function.apply(requestDto, listItemClass));
        writeMethod.invoke(host, propertyValue);

        return modelSaver.saveModel(host);
    }

    @SneakyThrows
    @Transactional
    public void removeFromCollection(CollectionCoordinates coordinates, Descriptor nestedId) {
        OwnedCoordinates owned = coordinates.getOwnedCoordinates();
        BasicModel host = retrieveHost(owned);
        BeanWrapper configurablePropertyAccessor = PropertyAccessorFactory.forBeanPropertyAccess(host);
        Collection<BasicModel<?>> propertyValue = (Collection<BasicModel<?>>) configurablePropertyAccessor.getPropertyValue(owned.getHostAttributeName());
        Method writeMethod = configurablePropertyAccessor.getPropertyDescriptor(owned.getHostAttributeName()).getWriteMethod();

        if (propertyValue == null) {
            throw new IllegalStateException("Can't remove item to collection");
        }

        propertyValue.removeIf(basicModel -> basicModel.getUuid().getExternalId().equals(nestedId.getExternalId()));
        writeMethod.invoke(host, propertyValue);

        modelSaver.saveModel(host);
    }
}