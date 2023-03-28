package io.extremum.tx.exceptions;


public class TransactionNotFoundException extends TransactionException {
    public TransactionNotFoundException(String txId) {
        super("Transaction with id " + txId + " was not found", 400);
    }
}
