package io.extremum.watch.end2end.fixture;

import io.extremum.common.iri.service.IriFacilities;
import io.extremum.mongo.dao.ReactiveMongoCommonDao;
import io.extremum.mongo.service.impl.ReactiveMongoCommonServiceImpl;
import io.extremum.watch.config.conditional.ReactiveWatchConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(ReactiveWatchConfiguration.class)
public class ReactiveWatchedModelServiceImpl
        extends ReactiveMongoCommonServiceImpl<WatchedModel>
        implements ReactiveWatchedModelService {
    public ReactiveWatchedModelServiceImpl(ReactiveMongoCommonDao<WatchedModel> dao, IriFacilities iriFacilities) {
        super(dao, iriFacilities);
    }
}
