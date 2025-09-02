package dev.miguel.api;

import dev.miguel.api.DTO.LogInDTO;
import dev.miguel.api.mapper.AuthenticationDtoMapper;
import dev.miguel.usecase.user.gateways.IAuthenticationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;


@Component
@RequiredArgsConstructor
@Log4j2
public class AuthenticationHandler {

    private final IAuthenticationUseCase authenticationUseCase;
    private final AuthenticationDtoMapper authenticationMapper;

    public Mono<ServerResponse> logIn(ServerRequest request) {
        log.info("--- Petición recibida en logIn ---");

        return request.bodyToMono(LogInDTO.class)
                .doOnNext(dto -> log.info("Iniciando sesión para: {}", dto.correoElectronico()))
                .flatMap(dto -> authenticationUseCase.login(dto.correoElectronico(), dto.contrasenia()))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response)
                )
                .doOnError(error -> log.error("Error en logIn", error));
    }


}
