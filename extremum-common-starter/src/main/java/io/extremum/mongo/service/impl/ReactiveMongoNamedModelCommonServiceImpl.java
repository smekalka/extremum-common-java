package io.extremum.mongo.service.impl;

import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.common.iri.service.IriFacilities;
import io.extremum.common.service.impl.ReactiveNamedModelCommonService;
import io.extremum.common.slug.SlugGenerationStrategy;
import io.extremum.mongo.dao.ReactiveMongoCommonDao;
import io.extremum.mongo.model.MongoNamedModel;
import io.extremum.mongo.service.ReactiveMongoCommonService;
import org.bson.types.ObjectId;

public abstract class ReactiveMongoNamedModelCommonServiceImpl<M extends MongoNamedModel> extends ReactiveNamedModelCommonService<ObjectId, M>
        implements ReactiveMongoCommonService<M> {

    public ReactiveMongoNamedModelCommonServiceImpl
            (ReactiveMongoCommonDao<M> dao,
             ReactiveDescriptorService reactiveDescriptorService,
             IriFacilities iriFacilities,
             SlugGenerationStrategy slugGenerationStrategy
            ) {
        super(dao, reactiveDescriptorService, iriFacilities, slugGenerationStrategy);
    }

    @Override
    protected ObjectId stringToId(String id) {
        return new ObjectId(id);
    }
}