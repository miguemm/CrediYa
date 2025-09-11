package dev.miguel.model.estado.gateways;

import dev.miguel.model.estado.Estado;
import reactor.core.publisher.Mono;

public interface EstadoRepository {

    Mono<Boolean> existsEstadoById(Long id);

    Mono<Estado> findEstadoById(Long id);
}
