package dev.miguel.api;

import dev.miguel.api.DTO.CreateUserDTO;
import dev.miguel.api.mapper.UserDtoMapper;
import dev.miguel.usecase.usuario.gateways.IUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Handler {

    private final IUserUseCase userUseCase;
    private final UserDtoMapper mapper;

    public Mono<ServerResponse> createUser(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CreateUserDTO.class)
                .map(mapper::toDomain)
                .flatMap(userUseCase::createUser)
                .map(mapper::toDto)
                .flatMap(savedDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedDto));
    }

    public Mono<ServerResponse> getUserById(ServerRequest serverRequest) {
        Long id = Long.valueOf(serverRequest.pathVariable("id"));

        return userUseCase.findUserById(id)
                .map(mapper::toDto)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

}
