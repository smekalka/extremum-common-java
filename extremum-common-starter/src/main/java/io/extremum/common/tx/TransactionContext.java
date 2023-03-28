package io.extremum.common.tx;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor

@Data
public class TransactionContext {
    private TransactionRequest transactionRequest;
    private String transactionId;
}