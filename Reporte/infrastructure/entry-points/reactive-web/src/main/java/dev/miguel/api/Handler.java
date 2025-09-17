package dev.miguel.api;

import dev.miguel.model.utils.exceptions.BusinessException;
import dev.miguel.model.utils.exceptions.ExceptionMessages;
import dev.miguel.model.utils.exceptions.ForbiddenException;
import dev.miguel.model.utils.userContext.UserContext;
import dev.miguel.model.utils.userContext.gateways.IExtractUserContext;
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
    private final IExtractUserContext extractUserContext;

    public Mono<ServerResponse> consultarMetricas(ServerRequest serverRequest) {
        log.info("--- Petición recibida en consultarMetricas ---");

        return extractUserContext.toUserContext(serverRequest.principal())
                .flatMap(userContext -> {
                    if (hasUnauthorizedRole(userContext)) {
                        return Mono.error(new ForbiddenException("Solo los Administradores pueden consultar las metricas."));
                    }

                    return Mono.empty();
                })
                .then(obtenerReporteUseCase.consultarMetricas())
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto)
                );
    }

    private boolean hasUnauthorizedRole(UserContext user) {
        return user == null
                || user.roles() == null
                || user.roles().stream().noneMatch(r -> "administrador".equalsIgnoreCase(r.trim()));
    }
}
