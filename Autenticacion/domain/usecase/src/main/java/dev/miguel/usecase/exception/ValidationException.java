package dev.miguel.usecase.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class ValidationException extends RuntimeException {
    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super("Campos inválidos");
        this.errors = errors;
    }
}
