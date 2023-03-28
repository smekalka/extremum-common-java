package io.extremum.common.iri.service;

import io.extremum.common.iri.factory.DefaultIriFactory;
import io.extremum.common.iri.factory.IriFactory;
import io.extremum.common.iri.factory.NamedIriFactory;
import io.extremum.common.iri.properties.IriProperties;
import io.extremum.common.model.NamedModel;
import io.extremum.sharedmodels.basic.Named;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DefaultIriFacilities implements IriFacilities {
    private final IriProperties iriProperties;
    private final ConcurrentHashMap<String, IriFactory> factories = new ConcurrentHashMap<>();
    private final IriFactory defaultIriFactory;

    public DefaultIriFacilities(IriProperties iriProperties) {
        this.iriProperties = iriProperties;
        defaultIriFactory = new DefaultIriFactory(iriProperties);
    }

    @Override
    @Nullable
    public IriFactory getIriFactory(Class<?> clazz) {
        List<Class<?>> allInterfacesForClass = new ArrayList<>(Arrays.asList(ClassUtils.getAllInterfacesForClass(clazz)));
        allInterfacesForClass.add(clazz);
        Set<IriFactory> foundFactories = new HashSet<>();

        for (Class<?> cl : allInterfacesForClass) {
            IriFactory foundFactory = factories.get(cl.getCanonicalName());
            if (foundFactory != null) {
                foundFactories.add(foundFactory);
            }
        }

        if (foundFactories.size() > 1) {
            throw new IllegalStateException(String.format("Found more than 1 available IriFactory for class: %s", clazz.getCanonicalName()));
        }

        if (foundFactories.size() == 1) {
            return foundFactories.stream().iterator().next();
        }

        return defaultIriFactory;
    }

    @Override
    public void register(Class<?> clazz, IriFactory candidate) {
        if (factories.values().stream().noneMatch(iriFactory -> iriFactory.getClass().getCanonicalName().equals(NamedIriFactory.class.getCanonicalName()))) {
            register(clazz.getCanonicalName(), candidate);
        }
    }

    private void register(String classCanonicalName, IriFactory candidate) {
        factories.put(classCanonicalName, candidate);
    }

    @PostConstruct
    private void registerDefaultIriFactories() {
        register(NamedModel.class.getCanonicalName(), new NamedIriFactory(iriProperties));
    }
}