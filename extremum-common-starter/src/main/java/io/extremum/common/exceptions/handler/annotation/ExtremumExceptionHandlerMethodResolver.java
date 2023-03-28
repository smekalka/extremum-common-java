package io.extremum.common.exceptions.handler.annotation;

import io.extremum.common.exceptions.ExceptionResponse;
import io.extremum.common.exceptions.ExtremumExceptionHandlers;
import org.springframework.core.ExceptionDepthComparator;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;

import static java.util.Comparator.comparingInt;

public class ExtremumExceptionHandlerMethodResolver {

    public static final ReflectionUtils.MethodFilter EXCEPTION_HANDLER_METHODS = method ->
            AnnotatedElementUtils.hasAnnotation(method, ExtremumExceptionHandler.class);

    private final Map<Class<? extends Throwable>, PriorityQueue<Method>> mappedMethods = new HashMap<>(16);

    private final Map<Class<? extends Throwable>, Method> exceptionLookupCache = new ConcurrentReferenceHashMap<>(16);

    public ExtremumExceptionHandlerMethodResolver(List<Class<? extends ExtremumExceptionHandlers>> handlerTypes) {
        for (Class<? extends ExtremumExceptionHandlers> handlerType : handlerTypes) {
            for (Method method : MethodIntrospector.selectMethods(handlerType, EXCEPTION_HANDLER_METHODS)) {
                for (Class<? extends Throwable> exceptionType : detectExceptionMappings(method)) {
                    Class<?> returnType = method.getReturnType();
                    if (returnType.isAssignableFrom(ExceptionResponse.class)) {
                        addExceptionMapping(exceptionType, method);
                    } else {
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        if (parameterTypes.length == 1) {
                            throw new IllegalStateException("Invalid return type for [" + parameterTypes[0].getSimpleName() +
                                    "] handler - expected: " + ExceptionResponse.class.getSimpleName()
                                    + ", actual: " + returnType.getSimpleName());
                        } else {
                            throw new IllegalStateException("Invalid return type for ExtremumExceptionHandler");
                        }
                    }
                }
            }
        }
    }

    private void addExceptionMapping(Class<? extends Throwable> exceptionType, Method method) {
        this.mappedMethods.computeIfAbsent(exceptionType, type -> new PriorityQueue<>(2, (m1, m2) -> {
            int compare = comparingInt(this::getOrder).compare(m1, m2);
            if (compare != 0) {
                return compare;
            }
            throw new IllegalStateException("Ambiguous @ExtremumExceptionHandler method mapped for " +
                    exceptionType.getSimpleName() + ": " + method.getDeclaringClass().getName() +":" +
                    method.getName() + ". " + "Use @Order to specify @ExtremumExceptionHandler.");
        })).add(method);
    }

    private int getOrder(Method method) {
        return Optional.ofNullable(AnnotatedElementUtils.findMergedAnnotation(method, Order.class))
                .map(Order::value)
                .orElse(0);
    }

    private List<Class<? extends Throwable>> detectExceptionMappings(Method method) {
        List<Class<? extends Throwable>> result = new ArrayList<>();
        detectAnnotationExceptionMappings(method);
        for (Class<?> paramType : method.getParameterTypes()) {
            if (Throwable.class.isAssignableFrom(paramType)) {
                result.add((Class<? extends Throwable>) paramType);
            }
        }
        if (result.isEmpty()) {
            throw new IllegalStateException("No exception types mapped to " + method);
        }
        return result;
    }

    private void detectAnnotationExceptionMappings(Method method) {
        ExtremumExceptionHandler ann = AnnotatedElementUtils.findMergedAnnotation(method, ExtremumExceptionHandler.class);
        Assert.state(ann != null, "No ExtremumExceptionHandler annotation");
    }

    public Method resolveMethodByThrowable(Throwable exception) {
        Method method = resolveMethodByExceptionType(exception.getClass());
        if (method == null) {
            Throwable cause = exception.getCause();
            if (cause != null) {
                method = resolveMethodByExceptionType(cause.getClass());
            }
        }
        return method;
    }

    private Method resolveMethodByExceptionType(Class<? extends Throwable> exceptionType) {
        Method method = this.exceptionLookupCache.get(exceptionType);
        if (method == null) {
            method = getMappedMethod(exceptionType);
            this.exceptionLookupCache.put(exceptionType, method);
        }
        return method;
    }

    private Method getMappedMethod(Class<? extends Throwable> exceptionType) {
        List<Class<? extends Throwable>> matches = new ArrayList<>();
        for (Class<? extends Throwable> mappedException : this.mappedMethods.keySet()) {
            if (mappedException.isAssignableFrom(exceptionType)) {
                matches.add(mappedException);
            }
        }
        if (!matches.isEmpty()) {
            matches.sort(new ExceptionDepthComparator(exceptionType));
            return this.mappedMethods.get(matches.get(0)).peek();
        } else {
            return null;
        }
    }

}
