package dev.miguel.usecase.user;

import dev.miguel.model.rol.gateways.RolRepository;
import dev.miguel.model.user.Token;
import dev.miguel.model.user.User;
import dev.miguel.model.user.gateways.UserRepository;
import dev.miguel.model.exception.UnauthorizedException;
import dev.miguel.model.exception.BusinessException;
import dev.miguel.model.exception.ExceptionMessages;
import dev.miguel.usecase.user.gateways.IUserUseCase;
import dev.miguel.usecase.user.validation.*;
import dev.miguel.model.user.gateways.ISecurityProvider;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UserUseCase implements IUserUseCase {

    private final UserRepository userRepository;
    private final RolRepository rolRepository;

    private final ISecurityProvider securityProvider;

    @Override
    public Mono<Void> createUser(User user) {
        UserValidator validator = new UserValidator();

        return validator.validateAll(user)
                .then(userRepository.findUserByEmail(user.getCorreoElectronico()).hasElement())
                .flatMap(emailExists -> {
                    if (emailExists) {
                        return Mono.error(new BusinessException(ExceptionMessages.CORREO_YA_EXISTE));
                    }
                    return rolRepository.existsById(user.getRolId());
                })
                .flatMap(rolExists -> {
                    if (!rolExists) {
                        return Mono.error(new BusinessException(ExceptionMessages.ROL_NO_EXISTE));
                    }

                    return securityProvider.encryptPassword(user)
                            .flatMap(encrypted -> {
                                user.setContrasenia(encrypted);
                                return userRepository.saveUser(user);
                            });
                })
                .then();
    }


    @Override
    public Mono<Token> login(String email, String password) {
        return userRepository.findUserByEmail(email)
                .switchIfEmpty(Mono.error(new UnauthorizedException(ExceptionMessages.USUARIO_CORREO_NO_EXISTE)))
                .flatMap(user ->
                        securityProvider.validatePassword(user, password)
                                .flatMap(validPassword -> {
                                    if (!validPassword) {
                                        return Mono.error(new UnauthorizedException(ExceptionMessages.USUARIO_CONTRASENIA_INCORRECTA));
                                    }

                                    return securityProvider.generateToken(user);
                                })
                );
    }


}
