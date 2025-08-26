package dev.miguel.api;

import dev.miguel.model.usuario.Usuario;
import dev.miguel.usecase.usuario.UsuarioUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Handler {
    private final UsuarioUseCase usuarioUseCase;

    public Mono<ServerResponse> listenSaveUsuario(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(Usuario.class)
                .flatMap(usuarioUseCase::saveUsuario)
                .flatMap(savedUsuario -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedUsuario));
    }

    public Mono<ServerResponse> listenUpdateUsuario(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(Usuario.class)
                .flatMap(usuarioUseCase::updateUsuario)
                .flatMap(savedUsuario -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedUsuario));
    }

    public Mono<ServerResponse> listenGetAllUsuarios(ServerRequest serverRequest) {
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(usuarioUseCase.getAllUsuarios(), Usuario.class);
    }

    public Mono<ServerResponse> listenGetUsuarioById(ServerRequest serverRequest) {
        Long id = Long.valueOf(serverRequest.pathVariable("id"));

        return usuarioUseCase.getUsuarioById(id)
                .flatMap(usuario -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(usuario))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> listenDeleteUsuario(ServerRequest serverRequest) {
        Long id = Long.valueOf(serverRequest.pathVariable("id"));

        return usuarioUseCase.deleteUsuario(id)
                .then(ServerResponse.noContent().build());
    }
}
