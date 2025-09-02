package dev.miguel.api;

import dev.miguel.api.DTO.CreateSolicitudDTO;
import dev.miguel.api.mapper.SolicitudDtoMapper;
import dev.miguel.model.exception.UnauthorizedException;
import dev.miguel.model.userContext.UserContext;
import dev.miguel.model.userContext.gateways.IExtractUserContext;
import dev.miguel.usecase.solicitud.gateways.ISolicitudUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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

        // Extraer user
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

}
