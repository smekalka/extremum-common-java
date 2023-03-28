package io.extremum.tx.jpa;


import io.extremum.common.tx.TransactionContext;
import io.extremum.common.tx.TransactionContextHolder;
import io.extremum.common.tx.TransactionRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NamedThreadLocal;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class TransactionContextEntityFactory {

    @Autowired
    private EntityManagerFactory emf;

    private final Map<String, EntityManagerHolder> emMap = new HashMap<>();

    private static final ThreadLocal<EntityManager> entityManagerThreadLocal = new NamedThreadLocal<>("Entity manager");

    private final Duration timeout;

    public TransactionContextEntityFactory(EntityManagerFactory emf, Duration timeout) {
        this.emf = emf;
        this.timeout = timeout;
        Timer timer = new Timer();
        timer.schedule(new CleanExpiredManagersTask(), 0, 1000L);
    }

    public EntityManager getEntityManager() {
        TransactionContext transactionContext = TransactionContextHolder.getContext();

        if (transactionContext != null) {
            if (!transactionContext.getTransactionRequest().equals(TransactionRequest.BEGIN) && emMap.get(transactionContext.getTransactionId()) == null) {
                throw new IllegalStateException(String.format("Transaction with id %s expired or does not exist", transactionContext.getTransactionId()));
            }
            EntityManagerHolder entityManagerHolder = emMap.computeIfAbsent(transactionContext.getTransactionId(), k -> {
                try {
                    return new EntityManagerHolder(emf.createEntityManager(), Instant.now());
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
            entityManagerHolder.setLastActive(Instant.now());

            return entityManagerHolder.getEntityManager();

        } else {
            if (entityManagerThreadLocal.get() == null || !entityManagerThreadLocal.get().isOpen()) {
                entityManagerThreadLocal.set(emf.createEntityManager());
            }
            return entityManagerThreadLocal.get();
        }
    }

    @AllArgsConstructor
    @Getter
    @Setter
    private static class EntityManagerHolder {
        private EntityManager entityManager;
        private Instant lastActive;
    }

    private class CleanExpiredManagersTask extends TimerTask {

        @Override
        public void run() {
            Instant now = Instant.now();
            Iterator<Map.Entry<String, EntityManagerHolder>> iterator = emMap.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, EntityManagerHolder> next = iterator.next();
                EntityManagerHolder value = next.getValue();
                if (now.minus(timeout).isAfter(value.lastActive)) {

                    value.getEntityManager().getTransaction().rollback();
                    value.getEntityManager().close();
                    log.debug("Removed expired transaction {}", next.getKey());
                    iterator.remove();
                }
            }
        }
    }
}
