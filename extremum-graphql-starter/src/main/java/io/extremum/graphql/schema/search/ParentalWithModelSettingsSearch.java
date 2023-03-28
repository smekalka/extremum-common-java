package io.extremum.graphql.schema.search;

import graphql.annotations.processor.exceptions.CannotCastMemberException;
import graphql.annotations.processor.retrievers.GraphQLObjectInfoRetriever;
import graphql.annotations.processor.searchAlgorithms.ParentalSearch;
import io.extremum.everything.services.models.ModelSettingsProvider;
import io.extremum.sharedmodels.basic.ModelSettings;
import lombok.AllArgsConstructor;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.List;

@AllArgsConstructor
public class ParentalWithModelSettingsSearch extends ParentalSearch {

    private final ModelSettingsProvider modelSettingsProvider;

    public ParentalWithModelSettingsSearch(GraphQLObjectInfoRetriever graphQLObjectInfoRetriever, ModelSettingsProvider modelSettingsProvider) {
        super(graphQLObjectInfoRetriever);
        this.modelSettingsProvider = modelSettingsProvider;
    }

    @Override
    public boolean isFound(Member member) throws CannotCastMemberException {
        boolean isVisible = true;
        Class<?> declaringClass = member.getDeclaringClass();
        Class<?> topLevelClass = SearchFacilities.getTopLevelClass(declaringClass);

        if (topLevelClass.equals(declaringClass)) {
            ModelSettings settings = modelSettingsProvider.getSettings(declaringClass);
            if (settings != null && member instanceof Field) {
                isVisible = settings.getProperties().getVisible().contains(member.getName());
            }
        } else {
            ModelSettings settings = modelSettingsProvider.getSettings(topLevelClass);
            if (settings != null && member instanceof Field) {
                List<String> visibleFields = SearchFacilities.getDeclaringClassSettings(declaringClass, topLevelClass, settings);
                if (visibleFields != null) {
                    isVisible = visibleFields.contains(Introspector.decapitalize(member.getName()));
                }
            }
        }

        return isVisible && super.isFound(member);
    }
}