package io.extremum.common.exceptions.handler.annotation;

import io.extremum.common.exceptions.ExceptionResponse;
import io.extremum.common.exceptions.ExtremumExceptionHandlers;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Slf4j
public class AnnotationBasedExtremumExceptionResolver implements ExtremumExceptionResolver {

    private final ExtremumExceptionHandlerMethodResolver resolver;
    private final Map<Class<? extends ExtremumExceptionHandlers>, ExtremumExceptionHandlers> handlersMap;

    public AnnotationBasedExtremumExceptionResolver(List<ExtremumExceptionHandlers> extremumExceptionHandlers) {
        this.handlersMap = extremumExceptionHandlers.stream()
                .collect(
                        toMap(
                                AnnotationBasedExtremumExceptionResolver::getUserClass,
                                Function.identity())
                );

        List<Class<? extends ExtremumExceptionHandlers>> handlerTypes = extremumExceptionHandlers.stream()
                .map(ExtremumExceptionHandlers::getClass)
                .collect(toList());
        this.resolver = new ExtremumExceptionHandlerMethodResolver(handlerTypes);
    }

    private static Class<? extends ExtremumExceptionHandlers> getUserClass(ExtremumExceptionHandlers extremumExceptionHandlers1) {
        return (Class<? extends ExtremumExceptionHandlers>) ClassUtils.getUserClass(extremumExceptionHandlers1.getClass());
    }

    @Override
    @SneakyThrows
    public ExceptionResponse handleException(Throwable throwable) {
        Method method = resolver.resolveMethodByThrowable(throwable);
        if (method == null) {
            log.debug("Unhandled exception occurred in ExtremumExceptionResolver: {}", throwable.getLocalizedMessage(), throwable);
            throw throwable;
        }
        log.debug("Exception has occurred and will be handled in ExtremumExceptionResolver: {}",
                throwable.getLocalizedMessage(), throwable);
        return executeHandler(method, throwable);
    }

    @SneakyThrows
    private ExceptionResponse executeHandler(Method method, Throwable throwable) {
        try {
            ExtremumExceptionHandlers handlers = handlersMap.get(method.getDeclaringClass());
            return (ExceptionResponse) method.invoke(handlers, throwable);
        } catch (InvocationTargetException | IllegalAccessException e) {
            log.error("Unable to invoke extremum exception handler for method {}", method.getName(), e);
            throw e;
        }
    }

}
