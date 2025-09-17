package dev.miguel.model.utils.exceptions;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    public BusinessException(String errorMessage) {
        super(errorMessage);
    }
}
