package com.yapily.jose.database;

/**
 * We bundle the Jose exception as Runtime, due to the design of the Spring converter
 */
public class JoseDatabaseException extends RuntimeException {
    public JoseDatabaseException() {
    }

    public JoseDatabaseException(String message) {
        super(message);
    }

    public JoseDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public JoseDatabaseException(Throwable cause) {
        super(cause);
    }

    public JoseDatabaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
