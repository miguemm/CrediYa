package dev.miguel.model.estado.gateways;

import reactor.core.publisher.Mono;

public interface EstadoRepository {
    Mono<Boolean> existsEstadoById(Long id);
}
