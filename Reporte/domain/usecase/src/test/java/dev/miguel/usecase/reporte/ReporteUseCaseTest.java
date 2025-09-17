package dev.miguel.usecase.reporte;

import dev.miguel.model.metrica.Metrica;
import dev.miguel.model.metrica.gateways.MetricaRepository;
import dev.miguel.model.utils.exceptions.BusinessException;
import dev.miguel.model.utils.exceptions.ExceptionMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReporteUseCaseTest {

    @Mock
    MetricaRepository metricaRepository;

    @InjectMocks
    ReporteUseCase useCase;

    // Helpers simples para evitar repetir builders
    private static Metrica metricaAprobados(int cantidad) {
        return Metrica.builder().metrica("aprobados_cantidad").cantidad(cantidad).build();
    }

    private static Metrica metricaMonto(BigDecimal monto) {
        return Metrica.builder().metrica("aprobados_monto_total").monto(monto).build();
    }

    @Nested
    @DisplayName("incrementarMetricaAprobados")
    class IncrementarAprobados {

        @Test
        @DisplayName("OK: existe → incrementa +1 y guarda")
        void existe_incrementaYGuarda() {
            var existente = metricaAprobados(3);

            when(metricaRepository.getById("aprobados_cantidad")).thenReturn(Mono.just(existente));
            when(metricaRepository.save(any(Metrica.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

            StepVerifier.create(useCase.incrementarMetricaAprobados())
                    .verifyComplete();

            var captor = ArgumentCaptor.forClass(Metrica.class);
            verify(metricaRepository).save(captor.capture());
            var guardada = captor.getValue();

            assertEquals("aprobados_cantidad", guardada.getMetrica());
            assertEquals(4, guardada.getCantidad());
            verify(metricaRepository, times(1)).getById("aprobados_cantidad");
            verify(metricaRepository, times(1)).save(any(Metrica.class));
            verifyNoMoreInteractions(metricaRepository);
        }

        @Test
        @DisplayName("Error: no existe → BusinessException y no guarda")
        void noExiste_error() {
            when(metricaRepository.getById("aprobados_cantidad")).thenReturn(Mono.empty());

            StepVerifier.create(useCase.incrementarMetricaAprobados())
                    .expectErrorSatisfies(err -> {
                        assertInstanceOf(BusinessException.class, err);
                        assertEquals(ExceptionMessages.METRICA_NO_EXISTE, err.getMessage());
                    })
                    .verify();

            verify(metricaRepository, times(1)).getById("aprobados_cantidad");
            verify(metricaRepository, never()).save(any(Metrica.class));
            verifyNoMoreInteractions(metricaRepository);
        }
    }

    @Nested
    @DisplayName("incrementarMetricaMonto")
    class IncrementarMonto {

        @Test
        @DisplayName("OK: existe → suma monto y guarda")
        void existe_sumaYGuarda() {
            var existente = metricaMonto(new BigDecimal("100.50"));
            var delta = new BigDecimal("50.25");

            when(metricaRepository.getById("aprobados_monto_total")).thenReturn(Mono.just(existente));
            when(metricaRepository.save(any(Metrica.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

            StepVerifier.create(useCase.incrementarMetricaMonto(delta))
                    .verifyComplete();

            var captor = ArgumentCaptor.forClass(Metrica.class);
            verify(metricaRepository).save(captor.capture());
            var guardada = captor.getValue();

            assertEquals("aprobados_monto_total", guardada.getMetrica());
            assertEquals(new BigDecimal("150.75"), guardada.getMonto()); // 100.50 + 50.25
            verify(metricaRepository, times(1)).getById("aprobados_monto_total");
            verify(metricaRepository, times(1)).save(any(Metrica.class));
            verifyNoMoreInteractions(metricaRepository);
        }

        @Test
        @DisplayName("Error: no existe → BusinessException y no guarda")
        void noExiste_error() {
            var delta = new BigDecimal("2500.00");

            when(metricaRepository.getById("aprobados_monto_total")).thenReturn(Mono.empty());

            StepVerifier.create(useCase.incrementarMetricaMonto(delta))
                    .expectErrorSatisfies(err -> {
                        assertInstanceOf(BusinessException.class, err);
                        assertEquals(ExceptionMessages.METRICA_NO_EXISTE, err.getMessage());
                    })
                    .verify();

            verify(metricaRepository, times(1)).getById("aprobados_monto_total");
            verify(metricaRepository, never()).save(any(Metrica.class));
            verifyNoMoreInteractions(metricaRepository);
        }
    }

}
