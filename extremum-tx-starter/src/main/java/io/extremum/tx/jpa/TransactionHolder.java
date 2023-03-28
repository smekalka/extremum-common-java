package io.extremum.tx.jpa;

import io.extremum.common.utils.WeakConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;

@Slf4j
public class TransactionHolder {

    public TransactionHolder(int ttl) {
        this.transactionDataMap = new WeakConcurrentHashMap<>(ttl);
        transactionDataMap.setAfterClean(
                (transactionId, transaction) -> {
                    EntityManager entityManager = transaction.getEntityManager();
                    if (entityManager != null && entityManager.isOpen()) {
                        if (entityManager.getTransaction().isActive()) {
                            log.info("Rollback expired transaction {}", transactionId);
                            entityManager.getTransaction().rollback();
                        }
                        log.info("Close entity manager {}", transactionId);
                        entityManager.close();
                    }
                }
        );
    }

    private final WeakConcurrentHashMap<String, Transaction> transactionDataMap;

    public void setEntityManger(String transactionId, EntityManager entityManager) {
        Transaction existingTransaction = transactionDataMap.get(transactionId);
        if (existingTransaction == null) {
            transactionDataMap.put(transactionId, new Transaction(entityManager, null));
        } else {
            existingTransaction.setEntityManager(entityManager);
        }
    }

    public EntityManager getEntityManager(String transactionId) {
        Transaction transaction = transactionDataMap.get(transactionId);
        if (transaction == null) {
            return null;
        }

        return transactionDataMap.get(transactionId).getEntityManager();
    }

    public void setTransactionData(String transactionId, Object data) {
        Transaction existingData = transactionDataMap.get(transactionId);
        if (existingData == null) {
            transactionDataMap.put(transactionId, new Transaction(null, data));
        } else {
            existingData.setTransactionData(data);
        }
    }

    public Object getTransactionData(String transactionId) {
        Transaction transaction = transactionDataMap.get(transactionId);
        if (transaction == null) {
            return null;
        }

        return transactionDataMap.get(transactionId).getTransactionData();
    }

    public void remove(String id) {
        log.info("Remove transaction {}", id);
        transactionDataMap.remove(id);
    }

    @Data
    @AllArgsConstructor
    private static class Transaction {
        private EntityManager entityManager;
        private Object transactionData;
    }
}
