package dev.miguel.api;

import dev.miguel.model.utils.exceptions.BusinessException;
import dev.miguel.model.utils.exceptions.ExceptionMessages;
import dev.miguel.usecase.reporte.gateways.IObtenerReporteUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Log4j2
public class Handler {

    private final IObtenerReporteUseCase obtenerReporteUseCase;

    public Mono<ServerResponse> consultarMetricas(ServerRequest serverRequest) {
        log.info("--- Petición recibida en consultarMetricas ---");

        return obtenerReporteUseCase.consultarMetricas()
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto))
                .onErrorResume(ex -> {
                    log.error("Error consultando métricas", ex);

                    return Mono.error(new BusinessException(ExceptionMessages.ERROR));
                });
    }
}
