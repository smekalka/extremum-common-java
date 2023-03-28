package io.extremum.common.descriptor.end2end.fixture;

import io.extremum.common.iri.service.IriFacilities;
import io.extremum.mongo.dao.ReactiveMongoCommonDao;
import io.extremum.mongo.service.impl.ReactiveMongoCommonServiceImpl;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ReactiveTestMongoServiceImpl extends ReactiveMongoCommonServiceImpl<TestMongoModel> implements ReactiveTestMongoService {

    private final ReactiveMongoCommonDao<TestMongoModel> dao;

    public ReactiveTestMongoServiceImpl(ReactiveMongoCommonDao<TestMongoModel> dao, IriFacilities iriFacilities) {
        super(dao, iriFacilities);
        this.dao = dao;
    }

    public Flux<TestMongoModel> getModels() {
        return dao.findAll();
    }

}
