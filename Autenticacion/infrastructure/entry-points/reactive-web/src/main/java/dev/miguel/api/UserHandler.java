package dev.miguel.api;

import dev.miguel.api.DTO.CreateUserDTO;
import dev.miguel.api.mapper.UserDtoMapper;
import dev.miguel.usecase.user.gateways.IUserUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@RequiredArgsConstructor
@Log4j2
public class UserHandler {

    private final IUserUseCase userUseCase;
    private final UserDtoMapper userDtoMapper;

    @PreAuthorize("hasAnyRole('asesor','administrador')")
    public Mono<ServerResponse> createUser(ServerRequest serverRequest) {
        log.info("--- Petición recibida en createUser ---");

        return serverRequest.bodyToMono(CreateUserDTO.class)
                .doOnNext(dto -> log.info("DTO recibido: {}", dto))
                .map(userDtoMapper::toDomain)
                .doOnNext(user -> log.info("Entidad mapeada a dominio: {}", user))
                .flatMap(user -> userUseCase.createUser(user).thenReturn(user))
                .flatMap(user -> {
                    var location = URI.create("/api/v1/usuario/" + user.getId());
                    return ServerResponse.created(location).build();
                })
                .doOnError(error -> log.error("Error en createUser", error));
    }

}
