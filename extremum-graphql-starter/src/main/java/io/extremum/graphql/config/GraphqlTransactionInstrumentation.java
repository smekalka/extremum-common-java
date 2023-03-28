package io.extremum.graphql.config;

import graphql.ExecutionResult;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.SimpleInstrumentationContext;
import graphql.execution.instrumentation.parameters.InstrumentationExecuteOperationParameters;
import io.extremum.dao.DataAccessExceptionTranslators;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

public class GraphqlTransactionInstrumentation extends SimpleInstrumentation {
    private final PlatformTransactionManager transactionManager;
    private final DataAccessExceptionTranslators dataAccessExceptionTranslators;

    public GraphqlTransactionInstrumentation(PlatformTransactionManager transactionManager, DataAccessExceptionTranslators dataAccessExceptionTranslators) {
        this.transactionManager = transactionManager;
        this.dataAccessExceptionTranslators = dataAccessExceptionTranslators;
    }

    @Override
    public InstrumentationContext<ExecutionResult> beginExecuteOperation(InstrumentationExecuteOperationParameters parameters) {
        TransactionTemplate tx = new TransactionTemplate(this.transactionManager);

        TransactionStatus status = this.transactionManager.getTransaction(tx);
        return SimpleInstrumentationContext.whenCompleted((t, e) -> {
            if (!t.getErrors().isEmpty() && e == null) {
                this.transactionManager.rollback(status);
            } else {
                try {
                    this.transactionManager.commit(status);
                } catch (DataAccessException dataAccessException){
                    throw dataAccessExceptionTranslators.translate(dataAccessException);
                }
            }
        });
    }
}