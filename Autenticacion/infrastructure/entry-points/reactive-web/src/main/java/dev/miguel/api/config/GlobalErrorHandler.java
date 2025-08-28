package dev.miguel.api.config;

import dev.miguel.usecase.exception.ArgumentException;
import dev.miguel.usecase.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(ArgumentException.class)
    public ProblemDetail handleValidation(ArgumentException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Solicitud inválida");
        pd.setDetail(ex.getMessage());
        pd.setType(URI.create("https://example.com/errors/validation"));
        pd.setProperty("errors", ex.getErrors());
        return pd;
    }

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleValidation(BusinessException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Solicitud inválida");
        pd.setDetail(ex.getMessage());
        pd.setType(URI.create("https://example.com/errors/validation"));
        pd.setProperty("errors", ex.getMessage());
        return pd;
    }
}
