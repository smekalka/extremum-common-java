package io.extremum.everything;

import io.extremum.everything.collection.Projection;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class ProjectionCuttingTest {
    private final List<Integer> list = Arrays.asList(1, 2, 3, 4);
    private Projection projection;

    @Test
    void whenNoBoundsAreSpecified_thenCutShouldReturnEverything() {
        projection = Projection.offsetLimit(null, null);

        assertThat(projection.cut(list), is(Arrays.asList(1, 2, 3, 4)));
    }

    @Test
    void whenOnlyOffsetIsSpecified_thenOffsetShouldBeRespected() {
        projection = Projection.offsetLimit(1, null);

        assertThat(projection.cut(list), is(Arrays.asList(2, 3, 4)));
    }

    @Test
    void whenOnlyLimitIsSpecified_thenLimitShouldBeRespected() {
        projection = Projection.offsetLimit(null, 2);

        assertThat(projection.cut(list), is(Arrays.asList(1, 2)));
    }

    @Test
    void whenOffsetAndLimitAreSpecified_thenCutShouldRespectBoth() {
        projection = Projection.offsetLimit(1, 2);

        assertThat(projection.cut(list), is(Arrays.asList(2, 3)));
    }

    @Test
    void whenOffsetPlusLimitExceedMax_thenCutShouldReturnTheRemainder() {
        projection = Projection.offsetLimit(2, 10);

        assertThat(projection.cut(list), is(Arrays.asList(3, 4)));
    }

    @Test
    void whenOffsetExceedsMax_thenCutShouldReturnAnEmptyList() {
        projection = Projection.offsetLimit(4, 10);

        assertThat(projection.cut(list), is(Collections.emptyList()));
    }

    @Test
    void whenSeeingOnlyLastElement_thenTheLastElementShouldBeReturned() {
        projection = Projection.offsetLimit(3, 10);

        assertThat(projection.cut(list), is(Collections.singletonList(4)));
    }

    @Test
    void whenOffsetIsNegative_thenItShouldBeTreatedAsZero() {
        projection = Projection.offsetLimit(-1, 2);

        assertThat(projection.cut(list), is(Arrays.asList(1, 2)));
    }
}
