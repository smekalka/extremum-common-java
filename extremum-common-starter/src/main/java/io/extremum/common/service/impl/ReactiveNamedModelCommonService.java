package io.extremum.common.service.impl;

import io.extremum.common.dao.ReactiveCommonDao;
import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.common.iri.factory.IriFactory;
import io.extremum.common.iri.service.IriFacilities;
import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.common.model.NamedModel;
import io.extremum.common.slug.SlugGenerationStrategy;
import io.extremum.sharedmodels.basic.Model;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public abstract class ReactiveNamedModelCommonService<ID extends Serializable, M extends NamedModel<ID>> extends ReactiveCommonServiceImpl<ID, M> {

    private final ReactiveDescriptorService reactiveDescriptorService;
    private final IriFacilities iriFacilities;
    private final SlugGenerationStrategy slugGenerationStrategy;

    public ReactiveNamedModelCommonService(ReactiveCommonDao<M, ID> dao,
                                           ReactiveDescriptorService reactiveDescriptorService,
                                           IriFacilities iriFacilities,
                                           SlugGenerationStrategy slugGenerationStrategy) {
        super(dao, iriFacilities);
        this.reactiveDescriptorService = reactiveDescriptorService;
        this.iriFacilities = iriFacilities;
        this.slugGenerationStrategy = slugGenerationStrategy;
    }

    @Override
    public Mono<M> save(M data) {
        IriFactory iriFactory = iriFacilities.getIriFactory(NamedModel.class);
        Objects.requireNonNull(data.getName(), "Name must not be null");
        List<String> slugs = new ArrayList<>();
        if (data.getSlug() != null) {
            slugs.add(data.getSlug());
        } else {
            slugs = slugGenerationStrategy.generate(data.getName().getText());
        }

        if (data.getSlug() == null) {
            AtomicReference<String> iri = new AtomicReference<>();
            Mono<List<String>> filteredSlugs = Flux
                    .fromIterable(slugs)
                    .filterWhen(slug -> {
                                data.setSlug(slug);
                                iri.set(iriFactory.create(data));
                                return reactiveDescriptorService.loadByIri(iri.get())
                                        .flatMapMany(descriptor -> Flux.fromIterable(Collections.singletonList(descriptor)))
                                        .collectList()
                                        .map(List::isEmpty);
                            }
                    )
                    .switchIfEmpty(
                            Mono.defer(() -> Mono.error(new IllegalStateException("Unable to generate iri. Please choose another name or specify slug manually")))
                    )
                    .take(1)
                    .collectList();

            return filteredSlugs.flatMap(
                    s -> {
                        data.setSlug(s.get(0));
                        data.setIri(iri.get());

                        return super.save(data);
                    }
            );
        } else {
            return super.save(data);
        }
    }

    @Override
    public Mono<M> save(M data, Model folder) {
        IriFactory iriFactory = iriFacilities.getIriFactory(NamedModel.class);
        Objects.requireNonNull(data.getName(), "Name must not be null");
        List<String> slugs = new ArrayList<>();
        if (data.getSlug() != null) {
            slugs.add(data.getSlug());
        } else {
            slugs = slugGenerationStrategy.generate(data.getName().getText());
        }

        if (data.getSlug() == null) {
            AtomicReference<String> iri = new AtomicReference<>();
            Mono<List<String>> filteredSlugs = Flux
                    .fromIterable(slugs)
                    .filterWhen(slug -> {
                                data.setSlug(slug);
                                iri.set(iriFactory.create(data, (BasicModel<?>) folder));
                                return reactiveDescriptorService.loadByIri(iri.get())
                                        .flatMapMany(descriptor -> Flux.fromIterable(Collections.singletonList(descriptor)))
                                        .collectList()
                                        .map(List::isEmpty);
                            }
                    )
                    .switchIfEmpty(
                            Mono.defer(() -> Mono.error(new IllegalStateException("Unable to generate iri. Please choose another name or specify slug manually")))
                    )
                    .take(1)
                    .collectList();

            return filteredSlugs.flatMap(
                    s -> {
                        data.setSlug(s.get(0));
                        data.setIri(iri.get());

                        return super.save(data);
                    }
            );
        } else {
            return super.save(data);
        }
    }
}
