package dev.miguel.usercase.user.validationTest;

import dev.miguel.model.user.User;
import dev.miguel.usecase.user.validation.SalarioBaseValidation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
class SalarioBaseValidationTest {

    @InjectMocks
    private SalarioBaseValidation salarioBaseValidation;

    @Test
    void whenSalarioIsNull_shouldError() {
        User user = new User();
        user.setSalarioBase(null);

        Mono<Void> result = salarioBaseValidation.validate(user);

        StepVerifier.create(result)
                .expectErrorMatches(t -> t instanceof IllegalArgumentException &&
                        t.getMessage().equals("El campo salario_base no puede ser nulo o vacío"))
                .verify();
    }

    @Test
    void whenSalarioIsNegative_shouldError() {
        User user = new User();
        user.setSalarioBase(new BigDecimal("-1"));

        Mono<Void> result = salarioBaseValidation.validate(user);

        StepVerifier.create(result)
                .expectErrorMatches(t -> t instanceof IllegalArgumentException &&
                        t.getMessage().equals("El campo salario_base debe estar entre 0 y 15.000.000"))
                .verify();
    }

    @Test
    void whenSalarioIsAboveMax_shouldError() {
        User user = new User();
        user.setSalarioBase(new BigDecimal("20000000"));

        Mono<Void> result = salarioBaseValidation.validate(user);

        StepVerifier.create(result)
                .expectErrorMatches(t -> t instanceof IllegalArgumentException &&
                        t.getMessage().equals("El campo salario_base debe estar entre 0 y 15.000.000"))
                .verify();
    }

    @Test
    void whenSalarioIsValid_shouldComplete() {
        User user = new User();
        user.setSalarioBase(new BigDecimal("5000000"));

        Mono<Void> result = salarioBaseValidation.validate(user);

        StepVerifier.create(result).verifyComplete();
    }
}
