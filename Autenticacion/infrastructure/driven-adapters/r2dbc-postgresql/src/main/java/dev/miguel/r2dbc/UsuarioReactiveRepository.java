package dev.miguel.r2dbc;

import dev.miguel.r2dbc.entity.UsuarioEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface UsuarioReactiveRepository extends ReactiveCrudRepository<UsuarioEntity, Long >, ReactiveQueryByExampleExecutor<UsuarioEntity> {

}
