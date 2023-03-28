package io.extremum.common.exceptions.handler.annotation;

import io.extremum.common.exceptions.ExceptionResponse;
import io.extremum.common.exceptions.ExtremumExceptionHandlers;
import io.extremum.sharedmodels.dto.Alert;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.function.Function;

import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ExtremumExceptionResolverTest {

    @Test
    void whenExceptionHandledByResolver_exceptionResponseShouldBeReturned() {
        Function<Exception, ExceptionResponse> handlerFunction = e -> ExceptionResponse.withMessageAndHttpStatus(
                e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        ExtremumExceptionResolver resolver = new AnnotationBasedExtremumExceptionResolver(singletonList(
                new ExtremumExceptionHandlers() {
                    @ExtremumExceptionHandler
                    public ExceptionResponse handle(IllegalStateException e) {
                        return handlerFunction.apply(e);
                    }
                }));
        IllegalStateException exception = new IllegalStateException("Illegal State Exception message");

        ExceptionResponse response = resolver.handleException(exception);

        assertExceptionResponse(handlerFunction.apply(exception), response);
    }

    @Test
    void whenHandlerDoesntExist_nullShouldBeReturned() {
        ExtremumExceptionResolver resolver = new AnnotationBasedExtremumExceptionResolver(singletonList(
                new ExtremumExceptionHandlers() {
                }));

        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(
                () -> resolver.handleException(new IllegalStateException("Illegal State Exception message")));
    }

    private void assertExceptionResponse(ExceptionResponse expected, ExceptionResponse actual) {
        assertThat(actual.getHttpStatus()).isEqualTo(expected.getHttpStatus());

        assertThat(actual.getData().getCode()).isEqualTo(expected.getData().getCode());
        assertThat(actual.getData().getResult()).isEqualTo(expected.getData().getResult());
        assertThat(actual.getData().getAlerts())
                .usingElementComparator(comparing(Alert::getMessage).thenComparing(Alert::getCode))
                .isEqualTo(expected.getData().getAlerts());
    }

}