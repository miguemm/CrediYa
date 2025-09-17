package dev.miguel.api;

import dev.miguel.api.config.ReportesPath;
import dev.miguel.model.metrica.ReporteMetricas;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final ReportesPath reportesPath;
    private final Handler handler;

    // =========================
    // GET /api/v1/solicitud
    // =========================
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/reportes",                 // ← literal (no puede leer el bean)
                    produces = { MediaType.APPLICATION_JSON_VALUE },
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "consultarMetricas",
                    operation = @Operation(
                            operationId = "consultarMetricas",
                            summary = "Obtiene las métricas de aprobados",
                            description = "Retorna la cantidad total de solicitudes aprobadas y el monto total aprobado.",
                            tags = { "Reportes" },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "OK",
                                            content = @Content(
                                                    mediaType = "application/json",
                                                    schema = @Schema(implementation = ReporteMetricas.class)
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "401",
                                            description = "No autenticado o token inválido.",
                                            content = @Content(mediaType = "application/json",
                                                    examples = @ExampleObject(value = """
                                                            { "message": "No autenticado" }
                                                        """
                                                    )
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "403",
                                            description = "No autorizado (solo administradores pueden consultar métricas).",
                                            content = @Content(mediaType = "application/json",
                                                    examples = @ExampleObject(value = """
                                                            { "message": "solo administradores pueden consultar métricas." }
                                                        """
                                                    )
                                            )
                                    ),
                            }
                    )
            )
    })
    @Bean public RouterFunction<ServerResponse> routerFunction() {
        return route()
                .GET(reportesPath.getReportes(), handler::consultarMetricas)
                .build();
    }
}
