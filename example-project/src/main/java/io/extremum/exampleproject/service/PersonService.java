package io.extremum.exampleproject.service;

import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.common.iri.service.IriFacilities;
import io.extremum.common.slug.DefaultSlugGenerationStrategy;
import io.extremum.exampleproject.model.Person;
import io.extremum.exampleproject.repository.PersonageRepository;
import io.extremum.mongo.service.impl.ReactiveMongoNamedModelCommonServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PersonService extends ReactiveMongoNamedModelCommonServiceImpl<Person> {

    public PersonService(
            PersonageRepository dao,
            ReactiveDescriptorService reactiveDescriptorService,
            IriFacilities iriFacilities
    ) {
        super(dao, reactiveDescriptorService, iriFacilities, new DefaultSlugGenerationStrategy(100));
    }
}