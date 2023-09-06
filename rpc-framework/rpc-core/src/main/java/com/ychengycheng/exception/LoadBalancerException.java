package com.ychengycheng.exception;

/**
 * @author Valar Morghulis
 * @Date 2023/9/6
 */
public class LoadBalancerException extends RuntimeException{
    /**
     * Constructs a new runtime exception with {@code null} as its detail message.  The cause is not
     * initialized, and may subsequently be initialized by a call to {@link #initCause}.
     */
    public LoadBalancerException() {
        super();
    }

    /**
     * Constructs a new runtime exception with the specified detail message. The cause is not
     * initialized, and may subsequently be initialized by a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for later retrieval by the
     *                {@link #getMessage()} method.
     */
    public LoadBalancerException(String message) {
        super(message);
    }

    /**
     * Constructs a new runtime exception with the specified cause and a detail message of
     * {@code (cause==null ? null : cause.toString())} (which typically contains the class and
     * detail message of {@code cause}).  This constructor is useful for runtime exceptions that are
     * little more than wrappers for other throwables.
     *
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()}
     *              method). (A {@code null} value is permitted, and indicates that the cause is
     *              nonexistent or unknown.)
     * @since 1.4
     */
    public LoadBalancerException(Throwable cause) {
        super(cause);
    }
}
