package dev.miguel.usecase.user.gateways;

import dev.miguel.model.user.Token;
import dev.miguel.model.user.User;
import dev.miguel.model.utils.userContext.UserDetails;
import reactor.core.publisher.Mono;

public interface IUserUseCase {

    Mono<Void> createUser(User user);

    Mono<UserDetails> getUserById(Long userId);

    Mono<Token> login(String email , String password);

}
