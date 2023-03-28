package io.extremum.graphql.model;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import io.extremum.common.spring.data.OffsetBasedPageRequest;
import lombok.Data;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class PagingAndSortingRequest {

    public static GraphQLScalarType type = GraphQLScalarType.newScalar()
            .name("PagingAndSortingRequest").description("Paging and Sorting request parameter").coercing(new GraphQlPagingAndSortingRequestCoercing()).build();
    @GraphQLField
    @Min(1)
    @Max(100)
    private int limit = 10;
    @GraphQLField
    @Min(0)
    private int offset;
    @GraphQLField
    private List<SortOrder> orders = Collections.emptyList();

    @NotNull
    public <T> Comparator<T> getComparator() {
        return (first, second) -> {
            CompareToBuilder compToBuild = new CompareToBuilder();
            this.getOrders().forEach(sc -> {
                String fieldName = sc.getProperty();
                Sort.Direction direction = sc.getDirection();
                Object fv1 = PropertyAccessorFactory.forDirectFieldAccess(first).getPropertyValue(fieldName);
                Object fv2 = PropertyAccessorFactory.forDirectFieldAccess(second).getPropertyValue(fieldName);
                if (direction.isAscending()) {
                    compToBuild.append(fv1, fv2);
                }
                if (direction.isDescending()) {
                    compToBuild.append(fv2, fv1);
                }
            });

            return compToBuild.toComparison();
        };
    }

    public Pageable getPageable() {
        Pageable pageable = new OffsetBasedPageRequest(this.getOffset(), this.getLimit());
        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(
                        this
                                .getOrders()
                                .stream()
                                .map(sortOrder -> new Sort.Order(sortOrder.getDirection(), sortOrder.getProperty()))
                                .collect(Collectors.toList())
                )
        );
    }

    // for code gen only
    private static class GraphQlPagingAndSortingRequestCoercing implements Coercing<GraphQLScalarType, GraphQLScalarType> {

        @Override
        public GraphQLScalarType serialize(@NotNull Object dataFetcherResult) throws CoercingSerializeException {
            throw new NotImplementedException();
        }

        @Override
        public @NotNull GraphQLScalarType parseValue(@NotNull Object input) throws CoercingParseValueException {
            throw new NotImplementedException();
        }

        @Override
        public @NotNull GraphQLScalarType parseLiteral(@NotNull Object input) throws CoercingParseLiteralException {
            throw new NotImplementedException();
        }
    }
}
