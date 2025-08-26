package dev.miguel.model.usuario.gateways;

import dev.miguel.model.usuario.Usuario;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UsuarioRepository {

    Mono<Usuario> save(Usuario task);

    Flux<Usuario> findAll();

    Mono<Usuario> findById(Long id);

    Mono<Void> deleteById(Long id);
}
