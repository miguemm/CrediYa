package dev.miguel.usecase.solicitud.gateways;

import dev.miguel.model.solicitud.Solicitud;
import dev.miguel.model.userContext.UserContext;
import reactor.core.publisher.Mono;

public interface ISolicitudUseCase {

    Mono<Void> createSolicitud(Solicitud solicitud, UserContext user);

}
