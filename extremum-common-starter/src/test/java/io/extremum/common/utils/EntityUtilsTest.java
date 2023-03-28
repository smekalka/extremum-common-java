package io.extremum.common.utils;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class EntityUtilsTest {
    @Test
    void givenClassIsAProxyClass_whenIsProxyClassIsCalled_thenTrueShouldBeReturned() {
        assertThat(EntityUtils.isProxyClass(AProxy$HibernateProxy$Tail.class), is(true));
    }

    @Test
    void givenClassIsNotAProxyClass_whenIsProxyClassIsCalled_thenFalseShouldBeReturned() {
        assertThat(EntityUtils.isProxyClass(Object.class), is(false));
    }

    private static class AProxy$HibernateProxy$Tail {
    }
}