package dev.miguel.model.user.gateways;

import dev.miguel.model.user.User;
import reactor.core.publisher.Mono;

public interface UserRepository {

    Mono<User> saveUser(User task);

    Mono<User> findUserById(Long id);

    Mono<User> findUserByEmail(String email);
}
