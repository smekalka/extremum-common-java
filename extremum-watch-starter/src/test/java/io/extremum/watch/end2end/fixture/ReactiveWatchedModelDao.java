package io.extremum.watch.end2end.fixture;

import io.extremum.mongo.dao.impl.SpringDataReactiveMongoCommonDao;
import io.extremum.watch.config.conditional.ReactiveWatchConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean(ReactiveWatchConfiguration.class)
public interface ReactiveWatchedModelDao extends SpringDataReactiveMongoCommonDao<WatchedModel> {
}
