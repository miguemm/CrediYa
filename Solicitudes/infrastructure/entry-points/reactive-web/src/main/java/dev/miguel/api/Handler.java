package dev.miguel.api;

import dev.miguel.api.DTO.CreateSolicitudDTO;
import dev.miguel.api.mapper.ParamMapper;
import dev.miguel.api.mapper.SolicitudDtoMapper;
import dev.miguel.model.utils.exception.UnauthorizedException;
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

        // Autenticación -> UserContext
        Mono<UserContext> userMono = req.principal()
                .switchIfEmpty(Mono.error(new UnauthorizedException("No autenticado")))
                .map(extractUserContext::toUserContext)
                .switchIfEmpty(Mono.error(new UnauthorizedException("Token inválido")));

        // Leer body
        Mono<CreateSolicitudDTO> dtoMono = req.bodyToMono(CreateSolicitudDTO.class)
                .switchIfEmpty(Mono.error(new UnauthorizedException("Body requerido")));

        // Zipear, mapear a dominio y ejecutar UC
        return Mono.zip(userMono, dtoMono)
                .doOnNext(t -> log.info("DTO recibido: {} | User: {}", t.getT2(), t.getT1()))
                .map(t -> solicitudDtoMapper.toDomain(t.getT2()))           // -> Solicitud
                .zipWith(userMono)                                          // -> (Solicitud, UserContext)
                .flatMap(t -> solicitudUseCase.createSolicitud(t.getT1(), t.getT2()))
                .doOnNext(s -> log.info("Entidad creada: {}", s))
                .then(ServerResponse.created(URI.create("/api/v1/solicitud/")).build());
    }

    public Mono<ServerResponse> listAll(ServerRequest req) {
        // Autenticación -> UserContext
        Mono<UserContext> userMono = req.principal()
                .switchIfEmpty(Mono.error(new UnauthorizedException("No autenticado")))
                .map(extractUserContext::toUserContext);

        // Params (todos opcionales para este handler)
        String correo = req.queryParam("correoElectronico").map(String::trim).filter(s -> !s.isEmpty()).orElse(null);
        Long tipoPrestamoId = req.queryParam("tipoPrestamoId").map(ParamMapper::toLongOrNull).orElse(null);
        Long estadoId = req.queryParam("estadoId").map(ParamMapper::toLongOrNull).orElse(null);
        int page = req.queryParam("page").map(ParamMapper::toIntOrNull).orElse(0);
        int size = req.queryParam("size").map(ParamMapper::toIntOrNull).orElse(10);

        return userMono.flatMap(user -> {
                    log.info("listAll params -> estadoId={}, correo={}, tipoPrestamoId={}, page={}, size={}",
                            estadoId, correo, tipoPrestamoId, page, size);

                    return solicitudUseCase.findAll(
                                    correo,
                                    tipoPrestamoId,
                                    estadoId,
                                    page,
                                    size,
                                    user
                            )
                            .flatMap(result -> ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(result)
                            );
                }
        );
    }

}
