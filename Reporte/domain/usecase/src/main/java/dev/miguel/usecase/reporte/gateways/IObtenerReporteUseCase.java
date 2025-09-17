package dev.miguel.usecase.reporte.gateways;

import dev.miguel.model.metrica.ReporteMetricas;
import reactor.core.publisher.Mono;

public interface IObtenerReporteUseCase {
    Mono<ReporteMetricas> consultarMetricas();
}
