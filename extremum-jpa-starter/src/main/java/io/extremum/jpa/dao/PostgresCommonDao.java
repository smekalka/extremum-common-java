package io.extremum.jpa.dao;

import io.extremum.common.dao.CommonDao;
import io.extremum.sharedmodels.basic.BasicModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

public interface PostgresCommonDao<M extends BasicModel<UUID>> extends CommonDao<M, UUID> {

    List<M> findAll();

    Page<M> findAll(Specification<M> specification, Pageable pageable);
}
