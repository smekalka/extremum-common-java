package io.extremum.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author rpuch
 */
class DataSecurityAnnotationsTest {
    @Test
    void givenAModelClassIsDirectlyAnnotatedWithNoDataSecurity_whenCheckingWhetherAnnotated_thenShouldReturnTrue() {
        assertTrue(DataSecurityAnnotations.annotatedWithNoDataSecurity(AnnotatedDirectly.class));
    }

    @Test
    void givenAModelClassIsNotAnnotatedWithNoDataSecurity_whenCheckingWhetherAnnotated_thenShouldReturnFalse() {
        assertFalse(DataSecurityAnnotations.annotatedWithNoDataSecurity(NotAnnotated.class));
    }

    @Test
    void givenAModelClassIsDirectlyAnnotatedWithNoDataSecurity_whenCheckingWhetherAnnotatedOnItsProxy_thenShouldReturnTrue() {
        assertTrue(DataSecurityAnnotations.annotatedWithNoDataSecurity(AProxy$HibernateProxy$Tail.class));
    }

    @NoDataSecurity
    private static class AnnotatedDirectly {
    }

    private static class NotAnnotated {
    }

    private static class AProxy$HibernateProxy$Tail extends AnnotatedDirectly {
    }
}