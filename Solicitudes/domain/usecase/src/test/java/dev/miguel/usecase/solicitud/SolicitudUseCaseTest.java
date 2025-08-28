package dev.miguel.usecase.solicitud;

import dev.miguel.model.estado.gateways.EstadoRepository;
import dev.miguel.model.solicitud.Solicitud;
import dev.miguel.model.solicitud.gateways.SolicitudRepository;
import dev.miguel.model.tipoprestamo.gateways.TipoPrestamoRepository;
import dev.miguel.usecase.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SolicitudUseCaseTest {

    @InjectMocks
    private SolicitudUseCase solicitudUseCase;

    @Mock
    private SolicitudRepository solicitudRepository;
    @Mock
    private EstadoRepository estadoRepository;
    @Mock
    private TipoPrestamoRepository tipoPrestamoRepository;

    private static final Long ESTADO_PENDIENTE_REVISION_ID = 1L;

    private Solicitud solicitud = Solicitud.builder()
            .id(1L)
            .monto(BigDecimal.valueOf(10000000))
            .plazo(1)
            .correoElectronico("mandresmosquera@gmail.com")
            .estadoId(1L)
            .tipoPrestamoId(1L)
            .build();

    @Test
    @DisplayName("Crear solicitud")
    void createUser_ok () {
        when(tipoPrestamoRepository.existsTipoPrestamoById(solicitud.getTipoPrestamoId()))
                .thenReturn(Mono.just(true));

        when(estadoRepository.existsEstadoById(ESTADO_PENDIENTE_REVISION_ID))
                .thenReturn(Mono.just(true));

        when(solicitudRepository.saveSolicitud(solicitud))
                .thenReturn(Mono.just(solicitud));

        StepVerifier.create(solicitudUseCase.createSolicitud(solicitud))
                .verifyComplete();
    }

    @Test
    @DisplayName("Crear solicitud - tipo prestamo no existe")
    void createUser_bad_tipo_usuario_no_existe () {
        when(tipoPrestamoRepository.existsTipoPrestamoById(solicitud.getTipoPrestamoId()))
                .thenReturn(Mono.just(false));

        when(estadoRepository.existsEstadoById(ESTADO_PENDIENTE_REVISION_ID))
                .thenReturn(Mono.just(true));

        StepVerifier.create(solicitudUseCase.createSolicitud(solicitud))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    @DisplayName("Crear solicitud - estado solicitud no existe")
    void createUser_bad_estado_solicitud_no_existe () {
        when(tipoPrestamoRepository.existsTipoPrestamoById(solicitud.getTipoPrestamoId()))
                .thenReturn(Mono.just(true));

        when(estadoRepository.existsEstadoById(ESTADO_PENDIENTE_REVISION_ID))
                .thenReturn(Mono.just(false));

        StepVerifier.create(solicitudUseCase.createSolicitud(solicitud))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    @DisplayName("Crear solicitud - estado solicitud y tipo prestamo no existe")
    void createUser_bad_estado_solicitud_tipo_prestamo_no_existe () {
        when(tipoPrestamoRepository.existsTipoPrestamoById(solicitud.getTipoPrestamoId()))
                .thenReturn(Mono.just(false));

        when(estadoRepository.existsEstadoById(ESTADO_PENDIENTE_REVISION_ID))
                .thenReturn(Mono.just(false));

        StepVerifier.create(solicitudUseCase.createSolicitud(solicitud))
                .expectError(BusinessException.class)
                .verify();
    }

}
