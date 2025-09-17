package dev.miguel.model.utils.exceptions;

import lombok.Getter;

@Getter
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String errorMessage) {
        super(errorMessage);
    }
}
