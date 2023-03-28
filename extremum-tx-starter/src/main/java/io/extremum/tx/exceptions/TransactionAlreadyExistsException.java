package io.extremum.tx.exceptions;


public class TransactionAlreadyExistsException extends TransactionException {
    public TransactionAlreadyExistsException(String txId) {
        super("Transaction with id " + txId + " already exists", 400);
    }
}