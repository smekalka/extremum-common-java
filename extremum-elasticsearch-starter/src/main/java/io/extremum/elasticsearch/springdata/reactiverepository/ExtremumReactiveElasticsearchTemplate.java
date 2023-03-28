package io.extremum.elasticsearch.springdata.reactiverepository;

import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import io.extremum.elasticsearch.springdata.repository.ExtremumRequestFactory;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.BulkOptions;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.SeqNoPrimaryTerm;
import org.springframework.data.mapping.model.ConvertingPropertyAccessor;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ExtremumReactiveElasticsearchTemplate extends ReactiveElasticsearchTemplate {
    private final ElasticsearchConverter converter;

    private final ExtremumRequestFactory customizedRequestFactory;

    public ExtremumReactiveElasticsearchTemplate(ReactiveElasticsearchClient client, ElasticsearchConverter converter) {
        super(client, converter);

        this.converter = converter;

        customizedRequestFactory = new ExtremumRequestFactory(converter);
    }

    // The following methods (#save() and #saveAll() are only overriden to support setting version, seq_no and primary_key
    // after indexing. When this functionality makes it to the standard spring-data-elasticsearch, these overrides
    // should be removed.

    @Override
    public <T> Mono<T> save(T entity, IndexCoordinates index) {

        Assert.notNull(entity, "Entity must not be null!");

        return maybeCallBeforeConvert(entity, index)
                .flatMap(entityAfterBeforeConversionCallback -> doIndex(entityAfterBeforeConversionCallback, index)) //
                .map(it -> {
                    T savedEntity = it.getT1();
                    IndexResponse indexResponse = it.getT2();
                    T idPopulated = populateIdIfNecessary(savedEntity, indexResponse.getId());
                    return populateVersionAndSeqNoPrimaryTermIfNecessary(idPopulated, indexResponse);
                }).flatMap(saved -> maybeCallAfterSave(saved, index));
    }

    private <T> T populateIdIfNecessary(T bean, @Nullable Object id) {
        if (id == null) {
            return bean;
        }

        ElasticsearchPersistentEntity<?> entity = getPersistentEntityFor(bean.getClass());
        ConvertingPropertyAccessor<T> propertyAccessor = new ConvertingPropertyAccessor<>(
                entity.getPropertyAccessor(bean), converter.getConversionService());

        ElasticsearchPersistentProperty idProperty = entity.getIdProperty();
        propertyAccessor.setProperty(idProperty, id);

        return bean;
    }

    private <T> Mono<Tuple2<T, IndexResponse>> doIndex(T entity, IndexCoordinates index) {

        IndexRequest request = customizedRequestFactory.indexRequest(getIndexQuery(entity), index);
        request = prepareIndexRequest(entity, request);
        return Mono.just(entity).zipWith(doIndex(request));
    }

    private IndexQuery getIndexQuery(Object value) {
        ElasticsearchPersistentEntity<?> entity = getPersistentEntityFor(value.getClass());
        ConvertingPropertyAccessor<?> propertyAccessor = new ConvertingPropertyAccessor<>(
                entity.getPropertyAccessor(value), converter.getConversionService());

        Object id = propertyAccessor.getProperty(entity.getIdProperty());
        IndexQuery query = new IndexQuery();

        if (id != null) {
            query.setId(id.toString());
        }
        query.setObject(value);

        boolean usingSeqNo = false;

        if (entity.hasSeqNoPrimaryTermProperty()) {
            SeqNoPrimaryTerm seqNoPrimaryTerm = (SeqNoPrimaryTerm) propertyAccessor.getProperty(
                    entity.getRequiredSeqNoPrimaryTermProperty());

            if (seqNoPrimaryTerm != null) {
                query.setSeqNo(seqNoPrimaryTerm.getSequenceNumber());
                query.setPrimaryTerm(seqNoPrimaryTerm.getPrimaryTerm());
                usingSeqNo = true;
            }
        }

        // seq_no and version are incompatible in the same request
        if (!usingSeqNo && entity.hasVersionProperty()) {

            Number version = (Number) propertyAccessor.getProperty(entity.getVersionProperty());

            if (version != null) {
                query.setVersion(version.longValue());
            }
        }

        return query;
    }

    private <T> T populateVersionAndSeqNoPrimaryTermIfNecessary(T bean, IndexResponse response) {
        if (bean instanceof ElasticsearchCommonModel) {
            ElasticsearchCommonModel model = (ElasticsearchCommonModel) bean;
            model.setVersion(response.getVersion());
            model.setSeqNoPrimaryTerm(new SeqNoPrimaryTerm(response.getSeqNo(), response.getPrimaryTerm()));
        }
        return bean;
    }

    @Override
    public <T> Flux<T> saveAll(Mono<? extends Collection<? extends T>> entitiesPublisher, IndexCoordinates index) {

        Assert.notNull(entitiesPublisher, "Entities must not be null!");

        return entitiesPublisher.flatMapMany(entities -> {
            return Flux.fromIterable(entities) //
                    .concatMap(entity -> maybeCallBeforeConvert(entity, index));
        }).collectList().map(Entities::new).flatMapMany(entities -> {
            if (entities.isEmpty()) {
                return Flux.empty();
            }

            return doBulkOperation(entities.indexQueries(), BulkOptions.defaultOptions(), index) //
                    .index().flatMap(indexAndResponse -> {
                        T savedEntity = entities.entityAt(indexAndResponse.getT1());
                        BulkItemResponse bulkItemResponse = indexAndResponse.getT2();

                        populateIdIfNecessary(savedEntity, bulkItemResponse.getResponse().getId());
                        populateVersionAndSeqNoPrimaryTermIfNecessary(savedEntity, bulkItemResponse.getResponse());

                        return maybeCallAfterSave(savedEntity, index);
                    });
        });
    }

    private class Entities<T> {
        private final List<T> entities;

        private Entities(List<T> entities) {
            Assert.notNull(entities, "entities cannot be null");

            this.entities = entities;
        }

        private boolean isEmpty() {
            return entities.isEmpty();
        }

        private List<IndexQuery> indexQueries() {
            return entities.stream().map(ExtremumReactiveElasticsearchTemplate.this::getIndexQuery).collect(
                    Collectors.toList());
        }

        private T entityAt(long index) {
            // it's safe to cast to int because the original indexed colleciton was fitting in memory
            int intIndex = (int) index;
            return entities.get(intIndex);
        }
    }
}
