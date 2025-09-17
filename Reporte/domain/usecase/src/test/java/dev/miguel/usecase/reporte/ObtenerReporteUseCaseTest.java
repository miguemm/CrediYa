package dev.miguel.usecase.reporte;

import dev.miguel.model.metrica.Metrica;
import dev.miguel.model.metrica.gateways.MetricaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ObtenerReporteUseCaseTest {
    @Mock
    MetricaRepository metricaRepository;

    @InjectMocks
    ObtenerReporteUseCase useCase;

    private static Metrica metricaCantidad(int cantidad) {
        return Metrica.builder().metrica("aprobados_cantidad").cantidad(cantidad).build();
    }

    private static Metrica metricaMonto(BigDecimal monto) {
        return Metrica.builder().metrica("aprobados_monto_total").monto(monto).build();
    }

    @Nested
    @DisplayName("consultarMetricas")
    class ConsultarMetricas {

        @Test
        @DisplayName("OK: ambas métricas existen")
        void ambasExisten() {
            when(metricaRepository.getById("aprobados_cantidad"))
                    .thenReturn(Mono.just(metricaCantidad(5)));
            when(metricaRepository.getById("aprobados_monto_total"))
                    .thenReturn(Mono.just(metricaMonto(new BigDecimal("1234.56"))));

            StepVerifier.create(useCase.consultarMetricas())
                    .assertNext(reporte -> {
                        assertEquals(5, reporte.getAprobadosCantidad());
                        assertEquals(new BigDecimal("1234.56"), reporte.getAprobadosMontoTotal());
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("OK: falta cantidad → usa 0")
        void faltaCantidad() {
            when(metricaRepository.getById("aprobados_cantidad"))
                    .thenReturn(Mono.empty()); // no existe
            when(metricaRepository.getById("aprobados_monto_total"))
                    .thenReturn(Mono.just(metricaMonto(new BigDecimal("500"))));

            StepVerifier.create(useCase.consultarMetricas())
                    .assertNext(reporte -> {
                        assertEquals(0, reporte.getAprobadosCantidad()); // por defecto
                        assertEquals(new BigDecimal("500"), reporte.getAprobadosMontoTotal());
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("OK: faltan ambas métricas → usa defaults")
        void faltanAmbas() {
            when(metricaRepository.getById("aprobados_cantidad")).thenReturn(Mono.empty());
            when(metricaRepository.getById("aprobados_monto_total")).thenReturn(Mono.empty());

            StepVerifier.create(useCase.consultarMetricas())
                    .assertNext(reporte -> {
                        assertEquals(0, reporte.getAprobadosCantidad());
                        assertEquals(BigDecimal.ZERO, reporte.getAprobadosMontoTotal());
                    })
                    .verifyComplete();
        }
    }
}
