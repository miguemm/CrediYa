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

import java.net.URI;

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
                    ServerResponse.created(URI.create("/api/v1/usuario" + savedDto.id()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedDto)
                )
                .doOnError(error -> log.error("Error en createUser", error));
    }

}
