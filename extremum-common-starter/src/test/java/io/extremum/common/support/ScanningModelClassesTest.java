package io.extremum.common.support;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.sharedmodels.basic.Model;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author rpuch
 */
class ScanningModelClassesTest {
    private final ModelClasses modelClasses = new ScanningModelClasses(
            ImmutableList.of(this.getClass().getPackage().getName()));

    @Test
    void givenAnEntityClassIsInAScannedPackage_whenGettingClassByModelName_thenItShouldBeFound() {
        Class<? extends Model> modelClass = modelClasses.getClassByModelName("FirstModel");

        assertThat(modelClass, is(sameInstance(FirstModel.class)));
    }

    @Test
    void givenAnEntityClassIsNotInAScannedPackage_whenGettingClassByModelName_thenAnExceptionShouldBeThrown() {
        try {
            modelClasses.getClassByModelName("NonExistingModel");
            fail("An exception should be thrown");
        } catch (ModelNotFoundException e) {
            assertThat(e.getMessage(), is(
                    "Model with name NonExistingModel is not known."));
        }
    }
}