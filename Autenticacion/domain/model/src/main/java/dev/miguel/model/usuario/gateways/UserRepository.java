package dev.miguel.model.usuario.gateways;

import dev.miguel.model.usuario.User;
import reactor.core.publisher.Mono;

public interface UserRepository {

    Mono<User> saveUser(User task);

    Mono<User> findUserById(Long id);
}
