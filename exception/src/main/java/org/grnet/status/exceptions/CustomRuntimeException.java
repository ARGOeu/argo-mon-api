package org.grnet.status.exceptions;

import java.util.Set;

public class CustomRuntimeException extends RuntimeException {

    private  int code ;
    private final Set<String> errors;

    public CustomRuntimeException(int code, String message, Set<String> errors) {
        super(message);
        this.errors = errors;
        this.code=code;
    }

    public int getCode() {
        return code;
    }

    public Set<String> getErrors() {
        return errors;
    }
}
