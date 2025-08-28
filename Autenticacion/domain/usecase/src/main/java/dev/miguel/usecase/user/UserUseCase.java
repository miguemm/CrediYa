package dev.miguel.usecase.user;

import dev.miguel.model.user.User;
import dev.miguel.model.user.gateways.UserRepository;
import dev.miguel.usecase.exception.BusinessException;
import dev.miguel.usecase.user.gateways.IUserUseCase;
import dev.miguel.usecase.user.validation.*;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UserUseCase implements IUserUseCase {

    private final UserRepository userRepository;

    @Override
    public Mono<User> createUser(User user) {
        UserValidator userValidator = new UserValidator();

        return userValidator.validateAll(user)
                .then(userRepository.findUserByEmail(user.getCorreoElectronico()))
                .flatMap(existing -> Mono.<User>error(new BusinessException("Correo ya existe")))
                .switchIfEmpty(Mono.defer(() -> userRepository.saveUser(user)));
    }

}
