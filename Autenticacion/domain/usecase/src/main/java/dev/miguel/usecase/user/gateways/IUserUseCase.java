package dev.miguel.usecase.user.gateways;

import dev.miguel.model.user.Token;
import dev.miguel.model.user.User;
import reactor.core.publisher.Mono;

public interface IUserUseCase {

    Mono<Void> createUser(User user);

    Mono<Token> login(String email , String password);

}
