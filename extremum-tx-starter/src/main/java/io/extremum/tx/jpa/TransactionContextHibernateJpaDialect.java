package io.extremum.tx.jpa;

import io.extremum.common.tx.TransactionContextHolder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.sql.SQLException;

@AllArgsConstructor
@Slf4j
public class TransactionContextHibernateJpaDialect extends HibernateJpaDialect {

    private final TransactionHolder transactionHolder;

    @Override
    public Object beginTransaction(EntityManager entityManager, TransactionDefinition definition) throws PersistenceException, SQLException, TransactionException {
        if (TransactionContextHolder.getContext() != null && TransactionContextHolder.getContext().getTransactionId() != null) {
            String transactionId = TransactionContextHolder.getContext().getTransactionId();

            Object existingTransactionData = transactionHolder.getTransactionData(transactionId);
            if (existingTransactionData != null) {
                if(!entityManager.getTransaction().isActive()){
                    Object transactionData = super.beginTransaction(entityManager, definition);
                    transactionHolder.setTransactionData(transactionId, transactionData);
                    log.info("Transaction data found, but transaction is not active. Begin transaction");

                    return transactionData;
                }
                return existingTransactionData;
            } else {
                Object transactionData = super.beginTransaction(entityManager, definition);
                transactionHolder.setTransactionData(transactionId, transactionData);
                return transactionData;
            }
        } else {
            return super.beginTransaction(entityManager, definition);
        }
    }
}