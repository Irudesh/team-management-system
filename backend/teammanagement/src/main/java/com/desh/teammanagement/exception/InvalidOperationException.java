package com.teammanagement.exception;

/**
 * Exception thrown when operation is invalid
 * Example: Assigning member to non-existent team
 */
public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException(String message) {
        super(message);
    }

    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}