package io.extremum.common.collection;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.OwnedCoordinates;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class OwnedCoordinatesTest {
    @Test
    void testToCoordinatesString() {
        OwnedCoordinates coordinates = new OwnedCoordinates(new Descriptor("external-id"), "items");
        assertThat(coordinates.toCoordinatesString(), is("OWNED/external-id/items"));
    }
}