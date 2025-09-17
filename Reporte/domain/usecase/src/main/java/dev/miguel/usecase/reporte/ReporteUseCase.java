package dev.miguel.usecase.reporte;

import dev.miguel.model.metrica.Metrica;
import dev.miguel.model.metrica.gateways.MetricaRepository;
import dev.miguel.usecase.reporte.gateways.IReporteUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ReporteUseCase implements IReporteUseCase {

    private final MetricaRepository metricaRepository;

    private static final String METRICA_APROBADOS = "aprobados_cantidad";

    @Override
    public Mono<Void> incrementar() {
        return metricaRepository.getById(METRICA_APROBADOS)
                .flatMap(metrica -> {
                    if (metrica == null) {
                        // si no existe, la creamos en 1
                        Metrica nueva = new Metrica(METRICA_APROBADOS, 1);
                        return metricaRepository.save(nueva);
                    } else {
                        metrica.setCantidad(metrica.getCantidad() + 1);
                        return metricaRepository.save(metrica);
                    }
                }).then();
    }
}
