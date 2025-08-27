package dev.miguel.usecase.usuario.gateways;

import dev.miguel.model.usuario.User;
import reactor.core.publisher.Mono;

public interface IUserUseCase {

    Mono<User> createUser(User user);

    Mono<User> findUserById(Long id);

}
