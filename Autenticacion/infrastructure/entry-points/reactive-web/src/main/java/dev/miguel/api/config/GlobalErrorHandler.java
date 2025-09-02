package dev.miguel.api.config;

import dev.miguel.api.DTO.ApiErrorResponse;
import dev.miguel.model.exception.ArgumentException;
import dev.miguel.model.exception.SecurityException;
import dev.miguel.model.exception.AuthorityException;
import dev.miguel.model.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(ArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidation(ArgumentException ex) {
        return new ApiErrorResponse(ex.getErrors());
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidation(BusinessException ex) {
        return new ApiErrorResponse(List.of(ex.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiErrorResponse handleValidation(SecurityException ex) {
        return new ApiErrorResponse(List.of(ex.getMessage()));
    }

    @ExceptionHandler(AuthorityException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleValidation(AuthorityException ex) {
        return new ApiErrorResponse(List.of(ex.getMessage()));
    }
}
