package io.extremum.exampleproject.repository;

import io.extremum.exampleproject.model.Person;
import io.extremum.mongo.dao.impl.SpringDataReactiveMongoCommonDao;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonageRepository extends SpringDataReactiveMongoCommonDao<Person> {
}