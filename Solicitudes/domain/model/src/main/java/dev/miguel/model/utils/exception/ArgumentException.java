package dev.miguel.model.utils.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class ArgumentException extends RuntimeException {
    private final List<String> errors;

    public ArgumentException(List<String> errors) {
        super("Campos inválidos");
        this.errors = errors;
    }
}
