package dev.miguel.usecase.reporte;

import dev.miguel.model.metrica.Metrica;
import dev.miguel.model.metrica.ReporteMetricas;
import dev.miguel.model.metrica.gateways.MetricaRepository;
import dev.miguel.usecase.reporte.gateways.IObtenerReporteUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Optional;

@RequiredArgsConstructor
public class ObtenerReporteUseCase implements IObtenerReporteUseCase {

    private final MetricaRepository metricaRepository;

    private static final String METRICA_APROBADOS = "aprobados_cantidad";
    private static final String METRICA_MONTO = "aprobados_monto_total";

    @Override
    public Mono<ReporteMetricas> consultarMetricas() {
        Mono<Metrica> cantidadMono = metricaRepository.getById(METRICA_APROBADOS)
                .defaultIfEmpty(Metrica.builder().metrica(METRICA_APROBADOS).cantidad(0).build());

        Mono<Metrica> montoMono = metricaRepository.getById(METRICA_MONTO)
                .defaultIfEmpty(Metrica.builder().metrica(METRICA_MONTO).monto(BigDecimal.ZERO).build());

        return Mono.zip(cantidadMono, montoMono)
                .map(tuple -> {
                    Metrica mCantidad = tuple.getT1();
                    Metrica mMonto    = tuple.getT2();

                    Integer cantidad  = Optional.ofNullable(mCantidad.getCantidad()).orElse(0);
                    BigDecimal monto  = Optional.ofNullable(mMonto.getMonto()).orElse(BigDecimal.ZERO);

                    return ReporteMetricas.builder()
                            .aprobadosCantidad(cantidad)
                            .aprobadosMontoTotal(monto)
                            .build();
                });
    }
}
