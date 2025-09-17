package dev.miguel.usecase.reporte;

import dev.miguel.model.metrica.gateways.MetricaRepository;
import dev.miguel.model.utils.exceptions.BusinessException;
import dev.miguel.model.utils.exceptions.ExceptionMessages;
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
                .switchIfEmpty(Mono.error(new BusinessException(ExceptionMessages.METRICA_NO_EXISTE)))
                .flatMap(m -> {
                    m.setCantidad(m.getCantidad() + 1);
                    return metricaRepository.save(m);
                })
                .then();
    }

    @Override
    public Mono<Void> incrementarMetricaMonto(BigDecimal monto) {
        return metricaRepository.getById(METRICA_MONTO)
                .switchIfEmpty(Mono.error(new BusinessException(ExceptionMessages.METRICA_NO_EXISTE)))
                .flatMap(m -> {
                    m.setMonto(m.getMonto().add(monto));
                    return metricaRepository.save(m);
                })
                .then();
    }
}
