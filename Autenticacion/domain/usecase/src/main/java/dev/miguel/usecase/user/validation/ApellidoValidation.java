package dev.miguel.usecase.user.validation;

import dev.miguel.model.user.User;
import reactor.core.publisher.Mono;

public class ApellidoValidation implements UserValidation {

    @Override
    public Mono<Void> validate(User user) {
        if (user.getApellidos() == null || user.getApellidos().isEmpty()) {
            return Mono.error(new IllegalArgumentException("El campo apellido no puede ser nulo o vacío"));
        }
        return Mono.empty();
    }
}