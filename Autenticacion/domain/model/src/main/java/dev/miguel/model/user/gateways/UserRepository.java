package dev.miguel.model.user.gateways;

import dev.miguel.model.user.Token;
import dev.miguel.model.user.User;
import reactor.core.publisher.Mono;

public interface UserRepository {

    Mono<User> saveUser(User user);

    Mono<User> findUserById(Long id);

    Mono<User> findUserByEmail(String email);

    Mono<Token> login(User user, String password);
}
