package io.extremum.common.tx;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NamedThreadLocal;

@Slf4j
public abstract class TransactionContextHolder {

    private static final ThreadLocal<TransactionContext> transactionContextHolder =
            new NamedThreadLocal<>("Transaction context");

    public static TransactionContext getContext(){
        return transactionContextHolder.get();
    }

    public static void setContext(TransactionContext context){
        transactionContextHolder.set(context);
        log.debug("Set transaction context {}", context);
    };

    public static void clear(){
        transactionContextHolder.remove();
    }
}
