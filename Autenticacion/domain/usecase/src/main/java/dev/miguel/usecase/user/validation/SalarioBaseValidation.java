package dev.miguel.usecase.user.validation;

import dev.miguel.model.user.User;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public class SalarioBaseValidation implements UserValidation {
    private static final BigDecimal SALARIO_MIN = BigDecimal.ZERO;                // 0
    private static final BigDecimal SALARIO_MAX = new BigDecimal("15000000");

    @Override
    public Mono<Void> validate(User user) {
        if (user.getSalario() == null) {
            return Mono.error(new IllegalArgumentException("El campo salario_base no puede ser nulo o vacío"));
        }

        if (user.getSalario().compareTo(SALARIO_MIN) < 0 || user.getSalario().compareTo(SALARIO_MAX) > 0) {
            return Mono.error(new IllegalArgumentException("El campo salario_base debe estar entre 0 y 15.000.000"));
        }
        return Mono.empty();
    }
}