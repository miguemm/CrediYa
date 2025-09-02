package dev.miguel.usecase.user;

import dev.miguel.model.user.Token;
import dev.miguel.model.user.gateways.UserRepository;
import dev.miguel.usecase.exception.AuthException;
import dev.miguel.usecase.exception.ExceptionMessages;
import dev.miguel.usecase.user.gateways.IAuthenticationUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class AuthenticationUseCase implements IAuthenticationUseCase {

    private final UserRepository userRepository;

    @Override
    public Mono<Token> login(String correoElectronico, String contrasenia) {
        return userRepository.findUserByEmail(correoElectronico)
                .switchIfEmpty(Mono.error(new AuthException(ExceptionMessages.USUARIO_CORREO_NO_EXISTE)))
                .flatMap(user -> userRepository.login(user, contrasenia))
                .switchIfEmpty(Mono.error(new AuthException(ExceptionMessages.USUARIO_CONTRASENIA_INCORRECTA)));
    }

}
