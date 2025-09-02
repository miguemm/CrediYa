package dev.miguel.usecase.exception;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {

    public AuthException(String errorMessage) {
        super(errorMessage);
    }
}
