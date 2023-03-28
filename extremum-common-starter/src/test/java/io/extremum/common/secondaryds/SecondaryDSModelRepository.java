package io.extremum.common.secondaryds;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
interface SecondaryDSModelRepository extends MongoRepository<ObjectId, SecondaryDSModel> {
}
