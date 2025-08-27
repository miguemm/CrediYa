package dev.miguel.usecase.user;

import dev.miguel.model.user.User;
import dev.miguel.model.user.gateways.UserRepository;
import dev.miguel.usecase.user.gateways.IUserUseCase;
import dev.miguel.usecase.user.validation.*;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class UserUseCase implements IUserUseCase {

    private final UserRepository userRepository;

    @Override
    public Mono<User> createUser(User user) {
        List<UserValidation> validations = List.of(
                new NombreValidation(),
                new ApellidoValidation(),
                new EmailValidation(userRepository),
                new SalarioBaseValidation());

        return UserValidationExecutor.validateAll(user, validations)
                .then(userRepository.saveUser(user));
    }

    @Override
    public Mono<User> findUserById(Long id) {
        return userRepository.findUserById(id);
    }

}
