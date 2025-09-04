package dev.miguel.model.utils.exception;

import lombok.Getter;

@Getter
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String errorMessage) {
        super(errorMessage);
    }
}
