package dev.miguel.usecase.user.validation;

import dev.miguel.model.user.User;
import reactor.core.publisher.Mono;

public interface UserValidation {
    Mono<Void> validate(User user);
}

