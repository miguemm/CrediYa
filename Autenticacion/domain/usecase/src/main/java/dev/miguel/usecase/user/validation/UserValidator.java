package dev.miguel.usecase.user.validation;

import dev.miguel.model.user.User;
import dev.miguel.usecase.exception.ArgumentException;
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
                        validateEmail(user)
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
                ? Mono.just("El campo nombre no puede ser nulo o vacío")
                : Mono.empty();
    }

    private Mono<String> validateApellido(User user) {
        return (user.getApellidos() == null || user.getApellidos().isEmpty())
                ? Mono.just("El campo apellido no puede ser nulo o vacío")
                : Mono.empty();
    }

    private Mono<String> validateSalario(User user) {
        return (user.getSalarioBase() == null || user.getSalarioBase().compareTo(SALARIO_MIN) < 0 || user.getSalarioBase().compareTo(SALARIO_MAX) > 0)
                ? Mono.just("El salario base debe estar entre 0 y 15.000.000")
                : Mono.empty();
    }

    private Mono<String> validateEmail(User user) {
        if (user.getCorreoElectronico() == null || user.getCorreoElectronico().isEmpty()) {
            return Mono.just("El campo correo_electronico no puede ser nulo o vacío");
        }

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (!user.getCorreoElectronico().matches(emailRegex)) {
            return Mono.just("El campo correo_electronico debe tener un formato de email válido");
        }

        return Mono.empty();
    }
}
