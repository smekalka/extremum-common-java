package io.extremum.jpa.service;


import io.extremum.common.service.CommonService;
import io.extremum.jpa.model.PostgresBasicModel;

/**
 * Common interface for basic posgres/JPA services.
 */
public interface PostgresBasicService<M extends PostgresBasicModel> extends CommonService<M> {
}
