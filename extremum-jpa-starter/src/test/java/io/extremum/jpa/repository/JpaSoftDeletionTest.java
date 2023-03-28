package io.extremum.jpa.repository;

import io.extremum.jpa.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author rpuch
 */
class JpaSoftDeletionTest {
    private final JpaSoftDeletion softDeletion = new JpaSoftDeletion();

    @Test
    void testStandardClasses() {
        assertTrue(softDeletion.supportsSoftDeletion(TestJpaModel.class));
        assertTrue(softDeletion.supportsSoftDeletion(SoftDeletePostgresModel.class));
        assertFalse(softDeletion.supportsSoftDeletion(HardDeleteJpaModel.class));
        assertFalse(softDeletion.supportsSoftDeletion(PostgresCommonModel.class));
        assertFalse(softDeletion.supportsSoftDeletion(PostgresBasicModel.class));
    }

    @Test
    void givenGetDeletedIsOverridenWithoutAnnotations_whenCheckingSoftDeletionSupport_thenItShouldBeSupported() {
        assertTrue(softDeletion.supportsSoftDeletion(GetDeletedOverridenWithoutAnnotations.class));
    }

    private static class GetDeletedOverridenWithoutAnnotations extends PostgresCommonModel {
        @Override
        public Boolean getDeleted() {
            return super.getDeleted();
        }
    }
}