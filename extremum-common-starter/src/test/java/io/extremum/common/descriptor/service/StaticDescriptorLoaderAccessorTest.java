package io.extremum.common.descriptor.service;

import io.extremum.sharedmodels.descriptor.DescriptorLoader;
import io.extremum.sharedmodels.descriptor.StaticDescriptorLoaderAccessor;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

/**
 * @author rpuch
 */
class StaticDescriptorLoaderAccessorTest {
    @Test
    void test() {
        DescriptorLoader originalLoader = StaticDescriptorLoaderAccessor.getDescriptorLoader();

        try {
            DescriptorLoader descriptorLoader = mock(DescriptorLoader.class);
            StaticDescriptorLoaderAccessor.setDescriptorLoader(descriptorLoader);

            assertThat(StaticDescriptorLoaderAccessor.getDescriptorLoader(), is(sameInstance(descriptorLoader)));
        } finally {
            StaticDescriptorLoaderAccessor.setDescriptorLoader(originalLoader);
        }
    }
}