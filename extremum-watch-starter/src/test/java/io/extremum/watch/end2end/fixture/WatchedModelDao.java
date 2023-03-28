package io.extremum.watch.end2end.fixture;

import io.extremum.mongo.dao.MongoCommonDao;
import io.extremum.mongo.dao.impl.SpringDataMongoCommonDao;
import io.extremum.watch.config.conditional.BlockingWatchConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

/**
 * @author rpuch
 */
@Repository
@ConditionalOnBean(BlockingWatchConfiguration.class)
public interface WatchedModelDao extends MongoCommonDao<WatchedModel>, SpringDataMongoCommonDao<WatchedModel> {
}
