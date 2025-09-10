package dev.miguel.api;

import dev.miguel.api.DTO.CreateSolicitudDTO;
import dev.miguel.api.mapper.ParamMapper;
import dev.miguel.api.mapper.SolicitudDtoMapper;
import dev.miguel.model.solicitud.Solicitud;
import dev.miguel.model.utils.exceptions.ForbiddenException;
import dev.miguel.model.utils.exceptions.UnauthorizedException;
import dev.miguel.model.utils.userContext.UserContext;
import dev.miguel.model.utils.userContext.gateways.IExtractUserContext;
import dev.miguel.usecase.solicitud.gateways.ISolicitudUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@RequiredArgsConstructor
@Log4j2
public class Handler {

    private final ISolicitudUseCase solicitudUseCase;
    private final SolicitudDtoMapper solicitudDtoMapper;
    private final IExtractUserContext extractUserContext;

    public Mono<ServerResponse> createSolicitud(ServerRequest req) {
        log.info("--- Petición recibida en createSolicitud ---");

        Mono<UserContext> authenticatedUser = extractUserContext.toUserContext(req.principal());

        Mono<CreateSolicitudDTO> solicitudDTO = req.bodyToMono(CreateSolicitudDTO.class)
                .switchIfEmpty(Mono.error(new UnauthorizedException("Body requerido")));

        return Mono.zip(authenticatedUser, solicitudDTO)
                .flatMap(tuple -> {
                    UserContext user = tuple.getT1();
                    CreateSolicitudDTO dto = tuple.getT2();

                    log.info("User: {} | Dto: {}", user, dto);

                    if (hasUnauthorizedRole(user, "cliente")) {
                        return Mono.error(new ForbiddenException("Solo los clientes pueden crear solicitudes."));
                    }

                    Solicitud solicitud = solicitudDtoMapper.toDomain(dto);
                    solicitud.setUsuarioId(Long.valueOf(user.id()));

                    return solicitudUseCase.createSolicitud(solicitud, user);
                })
                .then(ServerResponse.created(URI.create("/api/v1/solicitud/")).build());
    }

    public Mono<ServerResponse> listAll(ServerRequest req) {
        log.info("--- Petición recibida en listAll ---");

        Mono<UserContext> authenticatedUser = extractUserContext.toUserContext(req.principal());

        String correo = req.queryParam("correoElectronico").map(String::trim).filter(s -> !s.isEmpty()).orElse(null);
        Long tipoPrestamoId = req.queryParam("tipoPrestamoId").map(ParamMapper::toLongOrNull).orElse(null);
        Long estadoId = req.queryParam("estadoId").map(ParamMapper::toLongOrNull).orElse(null);
        int page = req.queryParam("page").map(ParamMapper::toIntOrNull).orElse(0);
        int size = req.queryParam("size").map(ParamMapper::toIntOrNull).orElse(10);

        return authenticatedUser
                .flatMap(user -> {
                    if (hasUnauthorizedRole(user, "asesor")) {
                        return Mono.error(new ForbiddenException("Solo los asesores pueden listar solicitudes."));
                    }

                    return solicitudUseCase.findAll(
                            correo,
                            tipoPrestamoId,
                            estadoId,
                            page,
                            size,
                            user
                    );
                })
                .flatMap(result -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(result)
                );
    }

    public Mono<ServerResponse> updateEstadoSolicitud(ServerRequest req) {
        log.info("--- Petición recibida en updateEstadoSolicitud ---");

        Long id = Long.valueOf(req.pathVariable("id"));

        return Mono.empty()
                .then(ServerResponse.ok().build());
    }


    private boolean hasUnauthorizedRole(UserContext user, String authorizedRol) {
        return user == null
                || user.roles() == null
                || user.roles().stream().noneMatch(r -> authorizedRol.equalsIgnoreCase(r.trim()));
    }

}
