package io.extremum.common.descriptor.service;

import io.extremum.sharedmodels.descriptor.DescriptorLoader;
import io.extremum.sharedmodels.descriptor.StaticDescriptorLoaderAccessor;
import lombok.RequiredArgsConstructor;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
public class StaticDescriptorLoaderAccessorConfigurator {
    private final DescriptorLoader descriptorLoader;

    @PostConstruct
    public void init() {
        StaticDescriptorLoaderAccessor.setDescriptorLoader(descriptorLoader);
    }

}
