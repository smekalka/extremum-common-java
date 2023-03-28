package io.extremum.graphql.schema.builder.field;

import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.retrievers.fieldBuilders.Builder;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.schema.GraphQLType;
import org.reactivestreams.Publisher;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

public class SubscriptionMethodTypeBuilder implements Builder<GraphQLType> {
    private Method method;
    private TypeFunction typeFunction;
    private ProcessingElementsContainer container;
    private boolean isInput;

    public SubscriptionMethodTypeBuilder(Method method, TypeFunction typeFunction, ProcessingElementsContainer container, boolean isInput) {
        this.method = method;
        this.typeFunction = typeFunction;
        this.container = container;
        this.isInput = isInput;
    }

    @Override
    public GraphQLType build() {
        AnnotatedType annotatedReturnType = method.getAnnotatedReturnType();
        if(!method.getReturnType().equals(Publisher.class)){
            throw new IllegalStateException(String.format("The GraphQLSubscription annotated method must return %s", Publisher.class));
        }

        Class<?> actualTypeArgument = (Class<?>)((ParameterizedType) this.method.getGenericReturnType()).getActualTypeArguments()[0];

        return this.typeFunction.buildType(isInput,actualTypeArgument, annotatedReturnType, container);
    }
}