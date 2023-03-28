package io.extremum.common.utils;

import io.extremum.common.model.annotation.HardDelete;
import io.extremum.common.annotation.ModelName;
import io.extremum.mongo.model.MongoCommonModel;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author rpuch
 */
class ModelUtilsTest {
    @Test
    void givenModelNameAnnotationExists_whenGetModelNameIsCalledWithClass_thenModelNameShouldBeReturned() {
        assertThat(ModelUtils.getModelName(Annotated.class), is("the-name"));
    }

    @Test
    void givenModelNameAnnotationExists_whenGetModelNameIsCalledWithObject_thenModelNameShouldBeReturned() {
        assertThat(ModelUtils.getModelName(new Annotated()), is("the-name"));
    }

    @Test
    void givenModelIsAProxyOfAnAnnotatedClass_whenGetModelNameIsCalledWithObject_thenModelNameShouldBeReturned() {
        assertThat(ModelUtils.getModelName(new AProxy$HibernateProxy$Tail()), is("the-name"));
    }

    @Test
    void givenNoModelNameAnnotationExists_whenGetModelNameIsCalled_thenAnExceptionShouldBeThrown() {
        try {
            ModelUtils.getModelName(NotAnnotated.class);
            fail("An exception should be thrown");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is(
                    "class io.extremum.common.utils.ModelUtilsTest$NotAnnotated is not annotated with @ModelName"));
        }
    }

    @Test
    void givenModelNameAnnotationExists_whenHasModelNameIsCalled_thenTrueShouldBeReturned() {
        assertThat(ModelUtils.hasModelName(Annotated.class), is(true));
    }

    @Test
    void givenNoModelNameAnnotationExists_whenHasModelNameIsCalled_thenFalseShouldBeReturned() {
        assertThat(ModelUtils.hasModelName(NotAnnotated.class), is(false));
    }

    @Test
    void givenModelIsAProxyOfAnAnnotatedClass_whenHasModelNameIsCalled_thenModelNameShouldBeReturned() {
        assertThat(ModelUtils.hasModelName(AProxy$HibernateProxy$Tail.class), is(true));
    }

    @Test
    void givenModelIsNotAnnotatedAsHardDelete_whenCheckingWhetherItIsSoftDeletable_thenTrueShouldBeReturned() {
        assertThat(ModelUtils.isSoftDeletable(NotAnnotatedAsHardDelete.class), is(true));
    }

    @Test
    void givenModelIsAnnotatedAsHardDelete_whenCheckingWhetherItIsSoftDeletable_thenFalseShouldBeReturned() {
        assertThat(ModelUtils.isSoftDeletable(AnnotatedAsHardDelete.class), is(false));
    }

    @Test
    void givenModelIsAnnotatedAsHardDelete_whenCheckingWhetherItsProxyIsSoftDeletable_thenFalseShouldBeReturned() {
        assertThat(ModelUtils.isSoftDeletable(AProxyWithHardDelete$HibernateProxy$Tail.class), is(false));
    }

    @ModelName("the-name")
    private static class Annotated extends MongoCommonModel {
    }

    private static class NotAnnotated extends MongoCommonModel {
    }

    private static class AProxy$HibernateProxy$Tail extends Annotated {
    }

    private static class NotAnnotatedAsHardDelete extends MongoCommonModel {
    }

    @HardDelete
    private static class AnnotatedAsHardDelete extends MongoCommonModel {
    }

    private static class AProxyWithHardDelete$HibernateProxy$Tail extends AnnotatedAsHardDelete {
    }
}