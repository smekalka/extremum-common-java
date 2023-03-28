package io.extremum.jpa.facilities;

import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

/**
 * @author rpuch
 */
class StaticPostgresDescriptorFacilitiesAccessorTest {
    private static final DescriptorSaver NOT_USED = null;

    @Test
    void test() {
        PostgresDescriptorFacilities originalFacilities = StaticPostgresDescriptorFacilitiesAccessor.getFacilities();

        try {
            PostgresDescriptorFacilities factory = new PostgresDescriptorFacilitiesImpl(
                    new DescriptorFactory(), NOT_USED, null);

            StaticPostgresDescriptorFacilitiesAccessor.setFacilities(factory);

            assertThat(StaticPostgresDescriptorFacilitiesAccessor.getFacilities(), is(factory));
        } finally {
            StaticPostgresDescriptorFacilitiesAccessor.setFacilities(originalFacilities);
        }
    }
}