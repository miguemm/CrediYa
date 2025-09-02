package dev.miguel.model.exception;

import lombok.Getter;

@Getter
public class SecurityException extends RuntimeException {

    public SecurityException(String errorMessage) {
        super(errorMessage);
    }
}
