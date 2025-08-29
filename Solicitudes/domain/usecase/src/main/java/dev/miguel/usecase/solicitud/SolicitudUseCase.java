package dev.miguel.usecase.solicitud;

import dev.miguel.model.estado.gateways.EstadoRepository;
import dev.miguel.model.solicitud.Solicitud;
import dev.miguel.model.solicitud.gateways.SolicitudRepository;
import dev.miguel.model.tipoprestamo.gateways.TipoPrestamoRepository;
import dev.miguel.usecase.exception.BusinessException;
import dev.miguel.usecase.solicitud.gateways.ISolicitudUseCase;
import dev.miguel.usecase.solicitud.validation.SolicitudValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SolicitudUseCase implements ISolicitudUseCase {

    private static final Long ESTADO_PENDIENTE_REVISION_ID = 1L;

    private final SolicitudRepository solicitudRepository;
    private final EstadoRepository estadoRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;

    @Override
    public Mono<Void> createSolicitud(Solicitud solicitud) {
        SolicitudValidator validator = new SolicitudValidator();

        return validator.validateAll(solicitud)
                .then(Mono.zip(
                        tipoPrestamoRepository.existsTipoPrestamoById(solicitud.getTipoPrestamoId()),
                        estadoRepository.existsEstadoById(ESTADO_PENDIENTE_REVISION_ID)
                ))
                .flatMap(exists -> {
                    boolean tipoOk = exists.getT1();
                    boolean estadoOk = exists.getT2();

                    if (!tipoOk) return Mono.error(new BusinessException("El tipo de préstamo no existe"));
                    if (!estadoOk) return Mono.error(new BusinessException("El estado de la solicitud no existe"));

                    solicitud.setEstadoId(ESTADO_PENDIENTE_REVISION_ID);
                    return solicitudRepository.saveSolicitud(solicitud).then();
                });
    }
}
