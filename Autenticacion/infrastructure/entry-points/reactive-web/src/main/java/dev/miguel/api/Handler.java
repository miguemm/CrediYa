package dev.miguel.api;

import dev.miguel.api.DTO.CreateUserDTO;
import dev.miguel.api.mapper.UserDtoMapper;
import dev.miguel.usecase.user.gateways.IUserUseCase;
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
public class Handler {

    private final IUserUseCase userUseCase;
    private final UserDtoMapper mapper;

    public Mono<ServerResponse> createUser(ServerRequest serverRequest) {
        log.info("--- Petición recibida en createUser ---");

        return serverRequest.bodyToMono(CreateUserDTO.class)
                .doOnNext(dto -> log.info("DTO recibido: {}", dto))
                .map(mapper::toDomain)
                .doOnNext(user -> log.info("Entidad mapeada a dominio: {}", user))
                .flatMap(userUseCase::createUser)
                .map(mapper::toDto)
                .flatMap(savedDto ->
                    ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedDto)
                )
                .doOnError(error -> log.error("Error en createUser", error));
    }

    public Mono<ServerResponse> getUserById(ServerRequest serverRequest) {
        Long id = Long.valueOf(serverRequest.pathVariable("id"));
        log.info("--- Petición recibida en getUserById con id = {} ---", id);

        return userUseCase.findUserById(id)
                .map(mapper::toDto)
                .doOnNext(dto -> log.info("Usuario mapeado a DTO: {}", dto))
                .flatMap(dto ->
                    ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto)
                )
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("No se encontró usuario con id={}", id);
                    return ServerResponse.notFound().build();
                }))
                .doOnError(error -> log.error("Error en getUserById para id={}", id, error));
    }

}
