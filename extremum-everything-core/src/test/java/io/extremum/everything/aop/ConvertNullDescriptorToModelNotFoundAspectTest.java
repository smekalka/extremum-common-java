package io.extremum.everything.aop;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.test.aop.AspectWrapping;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author rpuch
 */
class ConvertNullDescriptorToModelNotFoundAspectTest {
    private final ConvertNullDescriptorToModelNotFoundAspect aspect = new ConvertNullDescriptorToModelNotFoundAspect();
    private TestController controllerProxy;

    @BeforeEach
    void setUp() {
        controllerProxy = AspectWrapping.wrapInAspect(new TestController(), aspect);
    }

    @Test
    void whenInvokingWithNullDescriptor_thenModelNotFoundExceptionShouldBeThrown() {
        try {
            controllerProxy.methodWithDescriptor(null);
            fail("An exception should be thrown");
        } catch (ModelNotFoundException e) {
            assertThat(e.getMessage(), is("No descriptor was found"));
        }
    }

    @Test
    void whenInvokingWithNonNullDescriptor_thenNothingShouldBeThrown() {
        String result = controllerProxy.methodWithDescriptor(Descriptor.builder().externalId("id").build());
        assertThat(result, is("ok"));
    }

    @Test
    void whenInvokingWithNullObjectWhichIsNotADescriptor_thenNothingShouldBeThrown() {
        String result = controllerProxy.methodWithoutDescriptor(null);
        assertThat(result, is("ok"));
    }

    @Controller
    @ConvertNullDescriptorToModelNotFound
    @NoArgsConstructor
    private static class TestController {
        @SuppressWarnings("unused")
        String methodWithDescriptor(Descriptor descriptor) {
            return "ok";
        }

        @SuppressWarnings({"SameParameterValue", "unused"})
        String methodWithoutDescriptor(Object object) {
            return "ok";
        }
    }
}