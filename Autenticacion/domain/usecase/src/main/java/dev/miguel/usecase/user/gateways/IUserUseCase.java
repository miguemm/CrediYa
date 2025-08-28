package dev.miguel.usecase.user.gateways;

import dev.miguel.model.user.User;
import reactor.core.publisher.Mono;

public interface IUserUseCase {

    Mono<User> createUser(User user);

}
