package dev.miguel.api.config;

import dev.miguel.api.DTO.ApiErrorResponse;
import dev.miguel.model.utils.exception.ArgumentException;
import dev.miguel.model.utils.exception.BusinessException;
import dev.miguel.model.utils.exception.ForbiddenException;
import dev.miguel.model.utils.exception.UnauthorizedException;
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

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleValidation(UnauthorizedException ex) {
        return new ApiErrorResponse(List.of(ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiErrorResponse handleValidation(ForbiddenException ex) {
        return new ApiErrorResponse(List.of(ex.getMessage()));
    }
}
