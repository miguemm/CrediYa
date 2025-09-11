package dev.miguel.model.tipoprestamo.gateways;


import dev.miguel.model.tipoprestamo.TipoPrestamo;
import reactor.core.publisher.Mono;

public interface TipoPrestamoRepository {

    Mono<Boolean> existsTipoPrestamoById(Long id);

    Mono<TipoPrestamo> findTipoPrestamoById(Long id);

}
