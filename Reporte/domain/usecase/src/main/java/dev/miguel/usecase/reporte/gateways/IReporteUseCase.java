package dev.miguel.usecase.reporte.gateways;

import reactor.core.publisher.Mono;

public interface IReporteUseCase {
    Mono<Void> incrementar();
}
