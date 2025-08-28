package dev.miguel.model.rol.gateways;

import reactor.core.publisher.Mono;

public interface RolRepository {

    Mono<Boolean> existsById(Long id);
}
