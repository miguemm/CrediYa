package dev.miguel.model.exception;

import lombok.Getter;

@Getter
public class AuthorityException extends RuntimeException {

    public AuthorityException(String errorMessage) {
        super(errorMessage);
    }
}
