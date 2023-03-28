package io.extremum.tx.jpa;

import io.extremum.common.tx.TransactionContextHolder;
import io.extremum.common.tx.TransactionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.persistence.EntityManager;


@Slf4j
public class TransactionContextJpaTransactionManager extends JpaTransactionManager {

    public TransactionContextJpaTransactionManager(TransactionHolder transactionHolder) {
        super();
        this.transactionHolder = transactionHolder;
        this.jpaDialect = new TransactionContextHibernateJpaDialect(transactionHolder);
    }

    private final TransactionHolder transactionHolder;

    private final JpaDialect jpaDialect;

    @Override
    protected EntityManager createEntityManagerForTransaction() {
        if (TransactionContextHolder.getContext() == null) {
            return super.createEntityManagerForTransaction();
        } else {
            String transactionId = TransactionContextHolder.getContext().getTransactionId();
            EntityManager entityManager = transactionHolder.getEntityManager(transactionId);
            if (entityManager == null) {

                EntityManager entityManagerForTransaction = super.createEntityManagerForTransaction();
                transactionHolder.setEntityManger(transactionId, entityManagerForTransaction);
                log.info("Create new entity manager {} for transaction {}", entityManagerForTransaction, transactionId);

                return entityManagerForTransaction;
            } else {
                log.info("Found entity manager {} for transaction {}", entityManager, transactionId);
                if (!entityManager.getTransaction().isActive()) {
                    EntityManager entityManagerForTransaction = super.createEntityManagerForTransaction();
                    transactionHolder.setEntityManger(transactionId, entityManagerForTransaction);
                    log.info("Entity manager {} is closed create new for transaction {}:{}", entityManager, transactionId, entityManagerForTransaction);

                    return entityManagerForTransaction;
                }

                return entityManager;
            }
        }
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        if (TransactionContextHolder.getContext() == null || TransactionContextHolder.getContext() != null && TransactionContextHolder.getContext().getTransactionRequest().equals(TransactionRequest.COMMIT)) {
            super.doCommit(status);
        }
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        if (TransactionContextHolder.getContext() != null) {
            TransactionSynchronizationManager.unbindResource(obtainEntityManagerFactory());
        }

        if (TransactionContextHolder.getContext() == null || TransactionContextHolder.getContext() != null && TransactionContextHolder.getContext().getTransactionRequest().equals(TransactionRequest.COMMIT)) {
            if (TransactionContextHolder.getContext() != null) {
                log.info("Doing clean up for transaction {}", TransactionContextHolder.getContext().getTransactionId());
            }
            super.doCleanupAfterCompletion(transaction);

        } else {
            if (getDataSource() != null) {
                TransactionSynchronizationManager.unbindResource(getDataSource());
            }
        }
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        super.doBegin(transaction, definition);
    }

    @Override
    public JpaDialect getJpaDialect() {
        return jpaDialect;
    }
}
