package dev.miguel.usecase.reporte.gateways;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface IReporteUseCase {
    Mono<Void> incrementarMetricaAprobados();
    Mono<Void> incrementarMetricaMonto(BigDecimal monto);
}
