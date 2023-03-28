package io.extremum.rdf.triple.controller.dto;

import graphql.annotations.annotationTypes.GraphQLField;
import io.extremum.sharedmodels.basic.StringOrObject;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.sharedmodels.fundamental.CommonResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

@Getter
@AllArgsConstructor
public class TripleResponseDto extends CommonResponseDto {

    @GraphQLField
    private final String subject;

    @GraphQLField
    private final String predicate;

    @GraphQLField
    private final Collection<StringOrObject<ResponseDto>> objects;

    @Override
    public String getModel() {
        return "triples";
    }
}