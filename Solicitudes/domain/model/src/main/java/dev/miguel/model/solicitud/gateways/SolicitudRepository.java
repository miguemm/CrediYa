package dev.miguel.model.solicitud.gateways;

import dev.miguel.model.solicitud.Solicitud;
import reactor.core.publisher.Mono;

public interface SolicitudRepository {

    Mono<Solicitud> saveSolicitud(Solicitud solicitud);

}
