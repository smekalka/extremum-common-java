package io.extremum.common.secondaryds;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
interface ReactiveSecondaryDSModelRepository extends ReactiveMongoRepository<ObjectId, SecondaryDSModel> {
}
