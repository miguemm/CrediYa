package dev.miguel.model.rol.gateways;

import dev.miguel.model.rol.Rol;
import reactor.core.publisher.Mono;

public interface RolRepository {

    Mono<Boolean> existsById(Long id);

    Mono<Rol> findRolById(Long id);
}
