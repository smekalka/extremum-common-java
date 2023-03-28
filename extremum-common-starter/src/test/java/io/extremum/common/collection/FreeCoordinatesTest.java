package io.extremum.common.collection;

import io.extremum.sharedmodels.descriptor.FreeCoordinates;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class FreeCoordinatesTest {
    @Test
    void givenParamsIsNotNull_whenToCoordinatesStringIsCalled_thenBothNameAndParamsShouldBeOutput() {
        FreeCoordinates coordinates = new FreeCoordinates("id", "params");
        assertThat(coordinates.toCoordinatesString(), is("FREE/id/params"));
    }

    @Test
    void givenParamsIsNull_whenToCoordinatesStringIsCalled_thenOnlyNameShouldBeOutput() {
        FreeCoordinates coordinates = new FreeCoordinates("id");
        assertThat(coordinates.toCoordinatesString(), is("FREE/id"));
    }
}