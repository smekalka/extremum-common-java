package io.extremum.elasticsearch.springdata.reactiverepository;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchEntityInformation;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public class HardDeleteReactiveElasticsearchRepository<T extends ElasticsearchCommonModel>
        extends BaseReactiveElasticsearchRepository<T> {

    private final ElasticsearchEntityInformation<T, String> metadata;

    public HardDeleteReactiveElasticsearchRepository(
            ElasticsearchEntityInformation<T, String> metadata,
            ReactiveElasticsearchOperations elasticsearchOperations) {
        super(metadata, elasticsearchOperations);

        this.metadata = metadata;
    }

    @Override
    public Mono<T> deleteByIdAndReturn(String id) {
        return findById(id)
                .flatMap(found -> deleteById(id).thenReturn(found))
                .switchIfEmpty(Mono.error(new ModelNotFoundException(metadata.getJavaType(), id)));
    }
}
