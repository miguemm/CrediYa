package dev.miguel.usecase.usuario;

import dev.miguel.model.usuario.User;
import dev.miguel.model.usuario.gateways.UserRepository;
import dev.miguel.usecase.usuario.gateways.IUserUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UserUseCase implements IUserUseCase {

    private final UserRepository userRepository;

    @Override
    public Mono<User> createUser(User user) {
        return userRepository.saveUser(user);
    }

    @Override
    public Mono<User> findUserById(Long id) {
        return userRepository.findUserById(id);
    }
}
