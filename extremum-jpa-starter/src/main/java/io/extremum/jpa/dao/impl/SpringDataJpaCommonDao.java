package io.extremum.jpa.dao.impl;

import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.jpa.dao.PostgresCommonDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.UUID;

/**
 * @author rpuch
 */
@NoRepositoryBean
public interface SpringDataJpaCommonDao<M extends BasicModel<UUID>>
        extends PostgresCommonDao<M>, JpaRepository<M, UUID> {
}
