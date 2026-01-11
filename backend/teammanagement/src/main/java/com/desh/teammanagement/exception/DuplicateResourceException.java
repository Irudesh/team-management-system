package com.teammanagement.exception;

/**
 * Exception thrown when trying to create duplicate resource
 * Example: Creating team with name that already exists
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}