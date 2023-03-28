package io.extremum.elasticsearch.reactive;

import io.extremum.common.model.PersistableCommonModel;
import io.extremum.common.support.UniversalReactiveModelLoader;
import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import io.extremum.sharedmodels.descriptor.StorageType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@RequiredArgsConstructor
public class ElasticsearchUniversalReactiveModelLoader implements UniversalReactiveModelLoader {
    private final ReactiveElasticsearchOperations reactiveElasticsearchOperations;

    @Override
    public Mono<Model> loadByInternalId(String internalId, Class<? extends Model> modelClass) {
        return reactiveElasticsearchOperations.get(internalId, modelClass)
                .cast(ElasticsearchCommonModel.class)
                .filter(PersistableCommonModel::isNotDeleted)
                .map(Function.identity());
    }

    @Override
    public StorageType type() {
        return StandardStorageType.ELASTICSEARCH;
    }
}
