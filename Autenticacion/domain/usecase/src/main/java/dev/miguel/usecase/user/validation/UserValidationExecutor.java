package dev.miguel.usecase.user.validation;

import dev.miguel.model.user.User;
import dev.miguel.usecase.exception.ValidationException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

public class UserValidationExecutor {

    public static Mono<Void> validateAll(User user, List<UserValidation> validations) {
        return Flux.fromIterable(validations)
                .flatMap(v ->
                        v.validate(user)
                                .then(Mono.<String>empty())
                                .onErrorResume(e -> Mono.just(e.getMessage()))
                )
                .collectList()
                .flatMap(errors -> {
                    var real = errors.stream().filter(Objects::nonNull).toList();
                    return real.isEmpty()
                            ? Mono.empty()
                            : Mono.error(new ValidationException(real));
                });
    }
}
