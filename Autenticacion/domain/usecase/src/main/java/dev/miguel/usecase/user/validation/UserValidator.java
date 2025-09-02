package dev.miguel.usecase.user.validation;

import dev.miguel.model.user.User;
import dev.miguel.usecase.exception.ArgumentException;
import dev.miguel.usecase.exception.ExceptionMessages;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Objects;

@AllArgsConstructor
public class UserValidator {

    private static final BigDecimal SALARIO_MIN = BigDecimal.ZERO;                // 0
    private static final BigDecimal SALARIO_MAX = new BigDecimal("15000000");

    public Mono<Void> validateAll(User user) {
        return Flux.concat(
                    validateNombre(user),
                    validateApellido(user),
                    validateSalario(user),
                    validateEmail(user),
                    validateContrasenia(user)
                )
                .collectList()
                .flatMap(errors -> {
                    var real = errors.stream().filter(Objects::nonNull).toList();
                    return real.isEmpty()
                            ? Mono.empty()
                            : Mono.error(new ArgumentException(real));
                });
    }

    private Mono<String> validateNombre(User user) {
        return (user.getNombres() == null || user.getNombres().isEmpty())
                ? Mono.just(ExceptionMessages.CAMPO_NOMBRE_INVALIDO)
                : Mono.empty();
    }

    private Mono<String> validateApellido(User user) {
        return (user.getApellidos() == null || user.getApellidos().isEmpty())
                ? Mono.just(ExceptionMessages.CAMPO_APELLIDO_INVALIDO)
                : Mono.empty();
    }

    private Mono<String> validateSalario(User user) {
        return (user.getSalarioBase() == null || user.getSalarioBase().compareTo(SALARIO_MIN) < 0 || user.getSalarioBase().compareTo(SALARIO_MAX) > 0)
                ? Mono.just(ExceptionMessages.CAMPO_SALARIO_INVALIDO)
                : Mono.empty();
    }

    private Mono<String> validateEmail(User user) {
        if (user.getCorreoElectronico() == null || user.getCorreoElectronico().isEmpty()) {
            return Mono.just(ExceptionMessages.CAMPO_EMAIL_INVALIDO);
        }

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (!user.getCorreoElectronico().matches(emailRegex)) {
            return Mono.just(ExceptionMessages.FORMATO_EMAIL_INVALIDO);
        }

        return Mono.empty();
    }

    private Mono<String> validateContrasenia(User user) {
        return (user.getContrasenia() == null || user.getContrasenia().isEmpty())
                ? Mono.just(ExceptionMessages.CAMPO_CONTRASENIA_INVALIDO)
                : Mono.empty();
    }
}
