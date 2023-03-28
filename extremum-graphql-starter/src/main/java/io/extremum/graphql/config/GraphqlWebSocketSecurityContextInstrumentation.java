package io.extremum.graphql.config;

import graphql.ExecutionResult;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.SimpleInstrumentationContext;
import graphql.execution.instrumentation.parameters.InstrumentationExecuteOperationParameters;
import io.extremum.security.model.jwt.AuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.websocket.Session;

public class GraphqlWebSocketSecurityContextInstrumentation extends SimpleInstrumentation {

    @Override
    public InstrumentationContext<ExecutionResult> beginExecuteOperation(InstrumentationExecuteOperationParameters parameters) {
        Session session = parameters.getExecutionContext().getGraphQLContext().get(Session.class);
        if (session != null) {
            SecurityContextHolder.getContext().setAuthentication((AuthenticationToken) session.getUserPrincipal());
        }

        return SimpleInstrumentationContext.noOp();
    }
}
