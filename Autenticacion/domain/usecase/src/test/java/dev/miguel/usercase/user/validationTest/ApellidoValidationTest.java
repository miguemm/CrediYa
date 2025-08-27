package dev.miguel.usercase.user.validationTest;

import dev.miguel.model.user.User;
import dev.miguel.usecase.user.validation.ApellidoValidation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ApellidoValidationTest {

    @InjectMocks
    private ApellidoValidation apellidoValidation;

    @Test
    void whenApellidoIsNull_shouldError() {
        User user = new User();
        user.setApellidos(null);

        Mono<Void> result = apellidoValidation.validate(user);

        StepVerifier.create(result)
                .expectErrorMatches(t -> t instanceof IllegalArgumentException &&
                        t.getMessage().equals("El campo apellido no puede ser nulo o vacío"))
                .verify();
    }

    @Test
    void whenApellidoIsEmpty_shouldError() {
        User user = new User();
        user.setApellidos("");

        Mono<Void> result = apellidoValidation.validate(user);

        StepVerifier.create(result)
                .expectErrorMatches(t -> t instanceof IllegalArgumentException &&
                        t.getMessage().equals("El campo apellido no puede ser nulo o vacío"))
                .verify();
    }

    @Test
    void whenApellidoIsValid_shouldComplete() {
        User user = new User();
        user.setApellidos("Pérez");

        Mono<Void> result = apellidoValidation.validate(user);

        StepVerifier.create(result).verifyComplete();
    }
}
