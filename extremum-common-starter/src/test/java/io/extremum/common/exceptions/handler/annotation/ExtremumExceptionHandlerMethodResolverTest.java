package io.extremum.common.exceptions.handler.annotation;

import io.extremum.common.exceptions.ExceptionResponse;
import io.extremum.common.exceptions.ExtremumExceptionHandlers;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

class ExtremumExceptionHandlerMethodResolverTest {

    @Test
    void whenExceptionHasHandler_resolverShouldReturnThisHandler() throws NoSuchMethodException {
        ExtremumExceptionHandlers handlers = new ExtremumExceptionHandlers() {
            @ExtremumExceptionHandler
            public ExceptionResponse handle(IllegalStateException e) {
                return ExceptionResponse.withMessageAndHttpStatus(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };
        ExtremumExceptionHandlerMethodResolver resolver = new ExtremumExceptionHandlerMethodResolver(
                Collections.singletonList(handlers.getClass()));

        Method method = resolver.resolveMethodByThrowable(new IllegalStateException("Illegal State Exception"));

        assertThat(method).isEqualTo(handlers.getClass().getMethod("handle", IllegalStateException.class));
    }

    @Test
    void whenExceptionHasHandlerOfTypeAndHandlerOfSuperType_exactMatchedHandlerShouldBePreferred() throws NoSuchMethodException {
        ExtremumExceptionHandlers handlers = new ExtremumExceptionHandlers() {
            @ExtremumExceptionHandler
            public ExceptionResponse handle(IllegalStateException e) {
                return ExceptionResponse.withMessageAndHttpStatus(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            @ExtremumExceptionHandler
            public ExceptionResponse handle(Exception e) {
                return ExceptionResponse.withMessageAndHttpStatus("Should not be called", HttpStatus.MULTI_STATUS);
            }
        };
        ExtremumExceptionHandlerMethodResolver resolver = new ExtremumExceptionHandlerMethodResolver(
                Collections.singletonList(handlers.getClass()));

        Method method = resolver.resolveMethodByThrowable(new IllegalStateException("Illegal State Exception"));

        assertThat(method).isEqualTo(handlers.getClass().getMethod("handle", IllegalStateException.class));
    }

    @Test
    void whenHandlerOfExceptionTypeDoesntExist_resolverReturnsNull() {
        ExtremumExceptionHandlers handlers = new ExtremumExceptionHandlers() {
            @ExtremumExceptionHandler
            public ExceptionResponse handle(IllegalStateException e) {
                return ExceptionResponse.withMessageAndHttpStatus(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };
        ExtremumExceptionHandlerMethodResolver resolver = new ExtremumExceptionHandlerMethodResolver(
                Collections.singletonList(handlers.getClass()));

        Method method = resolver.resolveMethodByThrowable(new IllegalArgumentException("Illegal Argument Exception"));

        assertThat(method).isNull();
    }

    @Test
    void whenHandlerOfExceptionTypeDoesntExistButSuperTypeHandlerExists_superTypeHandlerShouldBeUsed() throws NoSuchMethodException {
        ExtremumExceptionHandlers handlers = new ExtremumExceptionHandlers() {
            @ExtremumExceptionHandler
            public ExceptionResponse handle(Exception e) {
                return ExceptionResponse.withMessageAndHttpStatus(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };
        ExtremumExceptionHandlerMethodResolver resolver = new ExtremumExceptionHandlerMethodResolver(
                Collections.singletonList(handlers.getClass()));

        Method method = resolver.resolveMethodByThrowable(new IllegalArgumentException("Illegal Argument Exception"));

        assertThat(method).isEqualTo(handlers.getClass().getMethod("handle", Exception.class));
    }

    @Test
    void whenHandlerReturnsIncorrectType_illegalStateExceptionShouldBeThrown() {
        ExtremumExceptionHandlers handlers = new ExtremumExceptionHandlers() {
            @ExtremumExceptionHandler
            public Integer handle(RuntimeException e) {
                return 5;
            }
        };
        assertThatIllegalStateException()
                .isThrownBy(() -> new ExtremumExceptionHandlerMethodResolver(Collections.singletonList(handlers.getClass())))
                .withMessage("Invalid return type for [RuntimeException] handler - expected: ExceptionResponse, actual: Integer");
    }

    @Test
    void whenMultipleHandlersHandleSameException_handlerWithHighestOrderShouldBeChosen() throws NoSuchMethodException {
        ExtremumExceptionHandlers defaultHandlers = new ExtremumExceptionHandlers() {
            @Order
            @ExtremumExceptionHandler
            public ExceptionResponse handle(RuntimeException e) {
                return ExceptionResponse.withMessageAndHttpStatus("Forbidden", HttpStatus.FORBIDDEN);
            }
        };

        ExtremumExceptionHandlers newHandlers = new ExtremumExceptionHandlers() {
            @Order(HIGHEST_PRECEDENCE)
            @ExtremumExceptionHandler
            public ExceptionResponse handle(RuntimeException e) {
                return ExceptionResponse.withMessageAndHttpStatus("Bad Gateway", HttpStatus.BAD_GATEWAY);
            }
        };

        ExtremumExceptionHandlerMethodResolver resolver = new ExtremumExceptionHandlerMethodResolver(Arrays.asList(
                defaultHandlers.getClass(), newHandlers.getClass()));

        Method method = resolver.resolveMethodByThrowable(new RuntimeException());

        assertThat(method).isEqualTo(newHandlers.getClass().getMethod("handle", RuntimeException.class));
    }

    @Test
    void whenMultipleHandlersHandleSameExceptionWithSameOrder_illegalStateExceptionShouldBeThrown() {
        ExtremumExceptionHandlers defaultHandlers = new ExtremumExceptionHandlers() {
            @Order(1)
            @ExtremumExceptionHandler
            public ExceptionResponse handle(RuntimeException e) {
                return ExceptionResponse.withMessageAndHttpStatus("Forbidden", HttpStatus.FORBIDDEN);
            }
        };

        ExtremumExceptionHandlers newHandlers = new ExtremumExceptionHandlers() {
            @Order(1)
            @ExtremumExceptionHandler
            public ExceptionResponse handle(RuntimeException e) {
                return ExceptionResponse.withMessageAndHttpStatus("Bad Gateway", HttpStatus.BAD_GATEWAY);
            }
        };

        assertThatIllegalStateException()
                .isThrownBy(() -> new ExtremumExceptionHandlerMethodResolver(Arrays.asList(
                        defaultHandlers.getClass(), newHandlers.getClass())))
                .withMessageContainingAll("Ambiguous @ExtremumExceptionHandler method mapped for RuntimeException:",
                        "Use @Order to specify @ExtremumExceptionHandler.");
    }

    @Test
    void whenMultipleHandlersHandleSameExceptionOneWithoutOrderAndAnotherWithLowestOrder_handlerWithoutOrderShouldBeTreatedAsZeroOrdered() throws NoSuchMethodException {
        ExtremumExceptionHandlers defaultHandlers = new ExtremumExceptionHandlers() {
            @Order
            @ExtremumExceptionHandler
            public ExceptionResponse handle(RuntimeException e) {
                return ExceptionResponse.withMessageAndHttpStatus("Forbidden", HttpStatus.FORBIDDEN);
            }
        };

        ExtremumExceptionHandlers newHandlers = new ExtremumExceptionHandlers() {
            @ExtremumExceptionHandler
            public ExceptionResponse handle(RuntimeException e) {
                return ExceptionResponse.withMessageAndHttpStatus("Bad Gateway", HttpStatus.BAD_GATEWAY);
            }
        };

        ExtremumExceptionHandlerMethodResolver resolver = new ExtremumExceptionHandlerMethodResolver(Arrays.asList(
                defaultHandlers.getClass(), newHandlers.getClass()));

        Method method = resolver.resolveMethodByThrowable(new RuntimeException());

        assertThat(method).isEqualTo(newHandlers.getClass().getMethod("handle", RuntimeException.class));
    }

}