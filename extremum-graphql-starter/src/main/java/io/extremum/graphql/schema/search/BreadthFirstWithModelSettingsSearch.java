package io.extremum.graphql.schema.search;

import graphql.annotations.annotationTypes.GraphQLTypeExtension;
import graphql.annotations.processor.exceptions.CannotCastMemberException;
import graphql.annotations.processor.retrievers.GraphQLObjectInfoRetriever;
import graphql.annotations.processor.searchAlgorithms.BreadthFirstSearch;
import io.extremum.everything.services.models.ModelSettingsProvider;
import io.extremum.sharedmodels.basic.ModelSettings;
import org.jetbrains.annotations.NotNull;

import java.beans.Introspector;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;

public class BreadthFirstWithModelSettingsSearch extends BreadthFirstSearch {

    private final ModelSettingsProvider modelSettingsProvider;

    public BreadthFirstWithModelSettingsSearch(GraphQLObjectInfoRetriever graphQLObjectInfoRetriever, ModelSettingsProvider modelSettingsProvider) {
        super(graphQLObjectInfoRetriever);
        this.modelSettingsProvider = modelSettingsProvider;
    }

    @Override
    public boolean isFound(Member member) throws CannotCastMemberException {
        boolean isVisible = true;
        Class<?> declaringClass = getMainType(member);
        Class<?> topLevelClass = SearchFacilities.getTopLevelClass(declaringClass);

        if (topLevelClass.equals(declaringClass)) {
            ModelSettings settings = modelSettingsProvider.getSettings(declaringClass);
            if (settings != null && member instanceof Method) {
                isVisible = settings.getProperties().getVisible().contains(Introspector.decapitalize(getPropertyName(member)));
            }
        } else {
            ModelSettings settings = modelSettingsProvider.getSettings(topLevelClass);
            if (settings != null && member instanceof Method) {
                List<String> visibleFields = SearchFacilities.getDeclaringClassSettings(declaringClass, topLevelClass, settings);
                if (visibleFields != null) {
                    isVisible = visibleFields.contains(Introspector.decapitalize(getPropertyName(member)));
                }
            }
        }

        return isVisible && super.isFound(member);
    }

    @NotNull
    private String getPropertyName(Member member) {
        if (member.getName().startsWith("is")) {
            return member.getName().substring(2);
        } else if (member.getName().startsWith("remove")) {
            return member.getName().substring(6);
        } else if (member.getName().startsWith("add") || member.getName().startsWith("get")) {
            return member.getName().substring(3);
        }

        return member.getName();
    }

    private Class<?> getMainType(Member member) {
        Class<?> declaringClass = member.getDeclaringClass();
        if (declaringClass.getAnnotation(GraphQLTypeExtension.class) != null) {
            return declaringClass.getAnnotation(GraphQLTypeExtension.class).value();
        }
        return declaringClass;
    }
}
