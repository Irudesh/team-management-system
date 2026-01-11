package com.teammanagement.exception;

/**
 * Exception thrown when a requested resource is not found
 * Example: GET /api/teams/999 (team doesn't exist)
 *
 * extends RuntimeException:
 *   - RuntimeException = unchecked exception (don't need to declare in method signature)
 *   - Exception = checked exception (must declare with 'throws')
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructor with custom message
     * Usage: throw new ResourceNotFoundException("Team not found with id: 5");
     */
    public ResourceNotFoundException(String message) {
        super(message);  // Pass message to parent RuntimeException
    }

    /**
     * Constructor with message and cause
     * Usage: throw new ResourceNotFoundException("Team not found", originalException);
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}