package io.extremum.rdf.triple.dao.jpa;

import io.extremum.jpa.dao.impl.SpringDataJpaCommonDao;
import io.extremum.rdf.triple.dao.jpa.model.Triple;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TripleJpaRepository extends SpringDataJpaCommonDao<Triple> {

    List<Triple> findAllBySubjectAndPredicate(String subject, String predicate, Pageable pageable);

    Triple findBySubjectAndPredicate(String subject, String predicate);


    List<Triple> findAllBySubject(String subject, Pageable pageable);

}
