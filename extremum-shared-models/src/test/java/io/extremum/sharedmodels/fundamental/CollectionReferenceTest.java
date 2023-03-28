package io.extremum.sharedmodels.fundamental;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

class CollectionReferenceTest {
    @Test
    void whenCreatingAnUninitializedCollectionReference_thenItsTopAndCountShouldBeNull() {
        CollectionReference<?> reference = CollectionReference.uninitialized();

        assertThat(reference.getTop(), is(nullValue()));
        assertThat(reference.getCount(), is(nullValue()));
    }

    @Test
    void whenCreatingACollectionReferenceWithCountOnly_thenItShouldBeFilledCorrectly() {
        CollectionReference<?> reference = CollectionReference.withTotal(10);

        assertThat(reference.getTop(), is(nullValue()));
        assertThat(reference.getCount(), is(10L));
    }
}