package io.extremum.elasticsearch.springdata.repository;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchEntityInformation;

/**
 * @author rpuch
 */
public class HardDeleteElasticsearchRepository<T extends ElasticsearchCommonModel>
        extends BaseElasticsearchRepository<T> {

    public HardDeleteElasticsearchRepository(
            ElasticsearchEntityInformation<T, String> metadata,
            ElasticsearchOperations elasticsearchOperations) {
        super(metadata, elasticsearchOperations);
    }

    @Override
    public T deleteByIdAndReturn(String id) {
        T model = findById(id).orElseThrow(() -> new ModelNotFoundException(entityClass, id));
        
        deleteById(id);

        // Do not change 'deleted' and 'modified' attributes here because for hard-deletion we
        // return the previous (before deletion) view of the model for all storage types.
        // If we change this, we need to change it everywhere.

        return model;
    }
}
