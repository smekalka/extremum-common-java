package common.dao.mongo.versioned;

import io.extremum.mongo.dao.impl.ReactiveMongoVersionedDaoImpl;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;

public class TestReactiveMongoVersionedDaoImpl extends ReactiveMongoVersionedDaoImpl<TestMongoVersionedModel>
        implements TestReactiveMongoVersionedDao {
    public TestReactiveMongoVersionedDaoImpl(ReactiveMongoOperations mongoOperations) {
        super(mongoOperations);
    }
}
