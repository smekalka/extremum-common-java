package io.extremum.graphql.model.relay;

import graphql.relay.Relay;
import graphql.schema.GraphQLArgument;
import io.extremum.graphql.model.PagingAndSortingRequest;

import java.util.ArrayList;
import java.util.List;

import static graphql.schema.GraphQLArgument.newArgument;

public class DefaultRelay extends Relay {

    @Override
    public List<GraphQLArgument> getConnectionFieldArguments() {
        List<GraphQLArgument> args = new ArrayList<>();
        args.add(newArgument()
                .name("paging")
                .description("fetching only nodes after this node (exclusive)")
                .type(PagingAndSortingRequest.type)
                .build());
        return args;
    }
}
