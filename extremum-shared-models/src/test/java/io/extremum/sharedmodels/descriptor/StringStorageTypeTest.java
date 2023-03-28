package io.extremum.sharedmodels.descriptor;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringStorageTypeTest {
    @Test
    void returnsItsStorageValue() {
        StorageType storageType = new StringStorageType("value");

        assertThat(storageType.getStorageValue(), is("value"));
    }

    @Test
    void matchesItsStringValue() {
        StorageType storageType = new StringStorageType("value");

        assertTrue(storageType.matches("value"));
    }
}