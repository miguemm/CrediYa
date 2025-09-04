package dev.miguel.model.user.gateways;

import dev.miguel.model.user.Token;
import dev.miguel.model.user.User;
import dev.miguel.model.utils.userContext.UserDetails;
import reactor.core.publisher.Mono;

public interface UserRepository {

    Mono<User> saveUser(User user);

    Mono<UserDetails> findUserById(Long id);

    Mono<User> findUserByEmail(String email);

}
