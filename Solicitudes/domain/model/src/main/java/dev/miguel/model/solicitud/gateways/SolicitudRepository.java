package dev.miguel.model.solicitud.gateways;

import dev.miguel.model.solicitud.Solicitud;
import dev.miguel.model.solicitud.proyections.SolicitudDto;
import dev.miguel.model.utils.page.PageModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SolicitudRepository {

    Mono<Solicitud> saveSolicitud(Solicitud solicitud);

    Mono<Solicitud> findSolicitudById(Long solicitudId);

    Flux<SolicitudDto> findAllSolicitudesAprobadasByUsuarioId(Long userId);

    Mono<PageModel<SolicitudDto>> findAll(
            String correo, Long tipoPrestamoId, Long estadoId, int page, int size
    );
}
