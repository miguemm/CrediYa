package dev.miguel.r2dbc;

import dev.miguel.model.usuario.Usuario;
import dev.miguel.model.usuario.gateways.UsuarioRepository;
import dev.miguel.r2dbc.entity.UsuarioEntity;
import dev.miguel.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class UsuarioRepositoryAdapter extends ReactiveAdapterOperations<
        Usuario,
        UsuarioEntity,
        Long,
        UsuarioReactiveRepository
> implements UsuarioRepository {

    public UsuarioRepositoryAdapter(UsuarioReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, entity -> mapper.map(entity, Usuario.class/* change for domain model */));
    }

    @Override
    public Mono<Usuario> save(Usuario usuario) {
        return super.save(usuario);
    }

    @Override
    public Flux<Usuario> findAll() {
        return super.findAll();
    }

    @Override
    public Mono<Usuario> findById(Long id) {
        return super.findById(id);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return repository.deleteById(id);
    }
}
