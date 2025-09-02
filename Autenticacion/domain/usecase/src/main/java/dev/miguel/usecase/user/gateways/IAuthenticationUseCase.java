package dev.miguel.usecase.user.gateways;

import dev.miguel.model.user.Token;
import reactor.core.publisher.Mono;

public interface IAuthenticationUseCase {

    Mono<Token> login(String correoElectronico, String contrasenia);

}
