package dev.miguel.usecase.solicitud.gateways;

import dev.miguel.model.solicitud.Solicitud;
import reactor.core.publisher.Mono;

public interface ISolicitudUseCase {

    Mono<Void> createSolicitud(Solicitud solicitud);

}
