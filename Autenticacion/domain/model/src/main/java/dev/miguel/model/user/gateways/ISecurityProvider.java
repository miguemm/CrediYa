package dev.miguel.model.user.gateways;

import dev.miguel.model.user.Token;
import dev.miguel.model.user.User;
import reactor.core.publisher.Mono;

public interface ISecurityProvider {

    Mono<String> encryptPassword(User user);

    Mono<Boolean> validatePassword(User user, String password);

    Mono<Token> generateToken (User user);
}
