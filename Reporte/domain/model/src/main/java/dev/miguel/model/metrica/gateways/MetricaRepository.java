package dev.miguel.model.metrica.gateways;

import dev.miguel.model.metrica.Metrica;
import reactor.core.publisher.Mono;

public interface MetricaRepository {
    Mono<Metrica> getById(String id);

    Mono<Metrica> save(Metrica model);
}
