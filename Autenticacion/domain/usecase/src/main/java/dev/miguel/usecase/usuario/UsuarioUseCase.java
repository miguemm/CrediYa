package dev.miguel.usecase.usuario;

import dev.miguel.model.usuario.Usuario;
import dev.miguel.model.usuario.gateways.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UsuarioUseCase {

    private final UsuarioRepository usuarioRepository;

    public Mono<Usuario> saveUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public Mono<Usuario> updateUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public Flux<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Mono<Usuario> getUsuarioById(Long id) {
        return usuarioRepository.findById(id);
    }

    public Mono<Void> deleteUsuario(Long id) {
        return usuarioRepository.deleteById(id);
    }
}
