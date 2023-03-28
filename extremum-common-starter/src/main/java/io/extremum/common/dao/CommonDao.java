package io.extremum.common.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface CommonDao<M, ID> {
    Optional<M> findById(ID id);

    boolean existsById(ID id);

    <N extends M> N save(N model);

    <N extends M> List<N> saveAll(Iterable<N> entities);

    void deleteById(ID id);

    M deleteByIdAndReturn(ID id);

    Page<M> findAll(Specification<M> specification, Pageable pageable);

    Page<M> findAll(Pageable pageable);

    Iterable<M> findAllById(Iterable<ID> ids);
}
