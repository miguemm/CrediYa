package dev.miguel.usecase.solicitud.gateways;

import dev.miguel.model.solicitud.Solicitud;
import dev.miguel.model.solicitud.proyections.SolicitudDto;
import dev.miguel.model.utils.page.PageModel;
import dev.miguel.model.utils.userContext.UserContext;
import reactor.core.publisher.Mono;

public interface ISolicitudUseCase {

    Mono<Void> createSolicitud(Solicitud solicitud, UserContext user);

    Mono<PageModel<SolicitudDto>> findAll(
            String correoElectronico,
            Long tipoPrestamoId,
            Long estadoId,
            Integer page,
            Integer size,
            UserContext user
    );
}
