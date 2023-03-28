package io.extremum.tx.jpa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import static org.mockito.Mockito.when;

class TransactionHolderTest {

    @Test
    void should_get_and_set_properly() {
        TransactionHolder transactionHolder = new TransactionHolder(3000);
        EntityManager mock = Mockito.mock(EntityManager.class);
        Object data = new Object();
        transactionHolder.setTransactionData("transaction-id", data);
        transactionHolder.setEntityManger("transaction-id", mock);

        Assertions.assertEquals(mock, transactionHolder.getEntityManager("transaction-id"));
        Assertions.assertEquals(data, transactionHolder.getTransactionData("transaction-id"));
    }

    @Test
    void should_close_expired_resources() {
        TransactionHolder transactionHolder = new TransactionHolder(100);
        EntityManager em = Mockito.mock(EntityManager.class);
        EntityTransaction tx = Mockito.mock(EntityTransaction.class);
        when(em.isOpen()).thenReturn(true);
        when(tx.isActive()).thenReturn(true);
        when(em.getTransaction()).thenReturn(tx);

        Object data = new Object();
        transactionHolder.setTransactionData("transaction-id", data);
        transactionHolder.setEntityManger("transaction-id", em);

        Assertions.assertEquals(em, transactionHolder.getEntityManager("transaction-id"));
        Assertions.assertEquals(data, transactionHolder.getTransactionData("transaction-id"));
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Assertions.assertNull(transactionHolder.getEntityManager("transaction-id"));
        Assertions.assertNull(transactionHolder.getTransactionData("transaction-id"));
        Mockito.verify(em, Mockito.times(1)).close();
        Mockito.verify(tx, Mockito.times(1)).rollback();

    }

    @Test
    void setTransactionData() {
    }

    @Test
    void getTransactionData() {
    }
}