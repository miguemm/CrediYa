package dev.miguel.model.utils.exceptions;

import lombok.Getter;

@Getter
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String errorMessage) {
        super(errorMessage);
    }
}
