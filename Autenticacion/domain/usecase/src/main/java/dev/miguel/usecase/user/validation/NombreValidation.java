package dev.miguel.usecase.user.validation;

import dev.miguel.model.user.User;
import reactor.core.publisher.Mono;

public class NombreValidation implements UserValidation {

    @Override
    public Mono<Void> validate(User user) {
        if (user.getNombres() == null || user.getNombres().isEmpty()) {
            return Mono.error(new IllegalArgumentException("El campo nombre no puede ser nulo o vacío"));
        }
        return Mono.empty();
    }
}