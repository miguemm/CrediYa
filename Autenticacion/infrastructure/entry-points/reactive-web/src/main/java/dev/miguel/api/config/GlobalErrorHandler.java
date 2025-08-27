package dev.miguel.api.config;

import dev.miguel.usecase.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleValidation(ValidationException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Solicitud inválida");
        pd.setDetail(ex.getMessage());
        pd.setType(URI.create("https://example.com/errors/validation"));
        pd.setProperty("errors", ex.getErrors()); // lista de mensajes
        return pd;
    }
}
