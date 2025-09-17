package dev.miguel.usecase.reporte;

import dev.miguel.model.metrica.Metrica;
import dev.miguel.model.metrica.gateways.MetricaRepository;
import dev.miguel.usecase.reporte.gateways.IReporteUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class ReporteUseCase implements IReporteUseCase {

    private final MetricaRepository metricaRepository;

    private static final String METRICA_APROBADOS = "aprobados_cantidad";
    private static final String METRICA_MONTO = "aprobados_monto_total";

    @Override
    public Mono<Void> incrementarMetricaAprobados() {
        return metricaRepository.getById(METRICA_APROBADOS)
                .flatMap(metrica -> {
                    if (metrica == null) {
                        Metrica nueva = Metrica.builder()
                                .metrica(METRICA_APROBADOS)
                                .cantidad(1)
                                .build();
                        return metricaRepository.save(nueva);
                    } else {
                        metrica.setCantidad(metrica.getCantidad() + 1);
                        return metricaRepository.save(metrica);
                    }
                }).then();
    }

    @Override
    public Mono<Void> incrementarMetricaMonto(BigDecimal monto) {
        return metricaRepository.getById(METRICA_MONTO)
                .flatMap(metrica -> {
                    if (metrica == null) {
                        Metrica nueva = Metrica.builder()
                                .metrica(METRICA_MONTO)
                                .monto(monto)
                                .build();
                        return metricaRepository.save(nueva);
                    } else {
                        metrica.setMonto(metrica.getMonto().add(monto));
                        return metricaRepository.save(metrica);
                    }
                }).then();
    }
}
