package dev.miguel.api;

import dev.miguel.api.DTO.CreateSolicitudDTO;
import dev.miguel.api.mapper.SolicitudDtoMapper;
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

    public Mono<ServerResponse> createSolicitud(ServerRequest serverRequest) {
        log.info("--- Petición recibida en createSolicitud ---");

        return serverRequest.bodyToMono(CreateSolicitudDTO.class)
                .doOnNext(dto -> log.info("DTO recibido: {}", dto))
                .map(solicitudDtoMapper::toDomain)
                .doOnNext(solicitud -> log.info("Entidad mapeada a dominio: {}", solicitud))
                .flatMap(solicitud -> solicitudUseCase.createSolicitud(solicitud).thenReturn(solicitud))
                .flatMap(solicitud -> {
                    var location = URI.create("/api/v1/solicitud/" + solicitud.getId());
                    return ServerResponse.created(location).build();
                })
                .doOnError(error -> log.error("Error en createSolicitud", error));
    }
}
