package dev.miguel.usercase.user.validationTest;

import dev.miguel.model.user.User;
import dev.miguel.usecase.user.validation.NombreValidation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class NombreValidationTest {

    @InjectMocks
    private NombreValidation nombreValidation;

    @Test
    void whenNombreIsNull_shouldError() {
        User user = new User();
        user.setNombres(null);

        Mono<Void> result = nombreValidation.validate(user);

        StepVerifier.create(result)
                .expectErrorMatches(t -> t instanceof IllegalArgumentException &&
                        t.getMessage().equals("El campo nombre no puede ser nulo o vacío"))
                .verify();
    }

    @Test
    void whenNombreIsEmpty_shouldError() {
        User user = new User();
        user.setNombres("");

        Mono<Void> result = nombreValidation.validate(user);

        StepVerifier.create(result)
                .expectErrorMatches(t -> t instanceof IllegalArgumentException &&
                        t.getMessage().equals("El campo nombre no puede ser nulo o vacío"))
                .verify();
    }

    @Test
    void whenNombreIsValid_shouldComplete() {
        User user = new User();
        user.setNombres("Pérez");

        Mono<Void> result = nombreValidation.validate(user);

        StepVerifier.create(result).verifyComplete();
    }
}
