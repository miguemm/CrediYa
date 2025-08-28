package dev.miguel.usecase.user;

import dev.miguel.model.rol.gateways.RolRepository;
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
    private final RolRepository rolRepository;

    @Override
    public Mono<Void> createUser(User user) {
        UserValidator validator = new UserValidator();

        return validator.validateAll(user)
                .then(userRepository.findUserByEmail(user.getCorreoElectronico()).hasElement())
                .flatMap(emailExists -> {
                    if (emailExists) {
                        return Mono.error(new BusinessException("Correo ya existe"));
                    }

                    return rolRepository.existsById(user.getRolId());
                })
                .flatMap(rolExists -> {
                    if (!rolExists) {
                        return Mono.error(new BusinessException("Rol no existe"));
                    }

                    return userRepository.saveUser(user);
                })
                .then();
    }

}
