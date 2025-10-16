package org.grnet.status.exceptions;

import java.util.Set;

public class BadRequestException extends RuntimeException {

    private final int code = 400;
    private final Set<String> errors;

    public BadRequestException(String message, Set<String> errors) {
        super(message);
        this.errors = errors;
    }

    public int getCode() {
        return code;
    }

    public Set<String> getErrors() {
        return errors;
    }
}
