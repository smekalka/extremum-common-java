package io.extremum.everything.services;

import io.extremum.sharedmodels.basic.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service that is used to obtain a model from the database for Everything-Everything GET operation
 * and PATCH operation.
 *
 * @param <M> model type
 */
public interface GetterService<M extends Model> extends EverythingEverythingService {
    /**
     * Search and returns a found object
     *
     * @param id descriptor internal ID
     * @return found object if its found or null otherwise
     */
    M get(String id);

    Page<M> getAll(Pageable pageable);

    List<M> getAllByIds(List<String> ids);
}
