package io.extremum.rdf.triple.dao.mongo;

import io.extremum.mongo.dao.impl.SpringDataReactiveMongoCommonDao;
import io.extremum.rdf.triple.dao.mongo.model.Triple;
import org.springframework.stereotype.Repository;

@Repository
public interface TripleMongoRepository extends SpringDataReactiveMongoCommonDao<Triple> {
}
