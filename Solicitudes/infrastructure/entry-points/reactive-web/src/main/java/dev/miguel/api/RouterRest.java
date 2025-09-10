package dev.miguel.api;

import dev.miguel.api.DTO.CreateSolicitudDTO;
import dev.miguel.api.config.SolicitudPath;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

    private final SolicitudPath solicitudPath;
    private final Handler handler;

    @RouterOperations({
            // =========================
            // POST /api/v1/solicitud
            // =========================
            @RouterOperation(
                    path = "/api/v1/solicitud",
                    produces = { MediaType.APPLICATION_JSON_VALUE },
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "createSolicitud",
                    operation = @Operation(
                            operationId = "createSolicitud",
                            summary = "Crear solicitud",
                            description = """
                    Crea una nueva solicitud con estado 'Pendiente de revisión'.
                    Requiere autenticación y rol **cliente**. El `correoElectronico` del body debe coincidir con el del usuario autenticado.
                    """,
                            tags = {"Solicitud - crear"},
                            security = { @SecurityRequirement(name = "bearerAuth") },
                            requestBody = @RequestBody(
                                    required = true,
                                    description = "Datos necesarios para crear la solicitud.",
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = CreateSolicitudDTO.class),
                                            examples = {
                                                    @ExampleObject(
                                                            name = "Ejemplo válido",
                                                            value = """
                                                                {
                                                                  "monto": 15000000.00,
                                                                  "plazo": 24,
                                                                  "correoElectronico": "cliente@dominio.com",
                                                                  "tipoPrestamoId": 2
                                                                }
                                                            """
                                                    )
                                            }
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "201",
                                            description = "Solicitud creada correctamente.",
                                            content = @Content,
                                            headers = @Header(
                                                    name = "Location",
                                                    description = "URI de la solicitud creada.",
                                                    schema = @Schema(type = "string", example = "/api/v1/solicitud/{id}")
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
                                            description = "No autorizado (solo clientes pueden crear solicitudes o email no coincide).",
                                            content = @Content(mediaType = "application/json",
                                                    examples = @ExampleObject(value = """
                                                            { "message": "Solo los clientes pueden crear solicitudes." }
                                                        """
                                                    )
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Error de validación de datos (campos requeridos o formato inválido).",
                                            content = @Content(mediaType = "application/json",
                                                    examples = @ExampleObject(value = """
                                                            {
                                                              "message": "Datos inválidos",
                                                              "errors": {
                                                                "monto": "Debe ser mayor a 0",
                                                                "plazo": "Debe ser mayor a 0"
                                                              }
                                                            }
                                                        """
                                                    )
                                            )
                                    )
                            }
                    )
            ),

            // =========================
            // GET /api/v1/solicitud
            // =========================
            @RouterOperation(
                    path = "/api/v1/solicitud",
                    produces = { MediaType.APPLICATION_JSON_VALUE },
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "listAll",
                    operation = @Operation(
                            operationId = "listSolicitudes",
                            summary = "Listar solicitudes (paginado y filtrado)",
                            description = """
                    Devuelve una página de solicitudes. Requiere autenticación y rol **asesor**.
                    Filtros opcionales: correo, tipo de préstamo, estado. Los resultados incluyen info adicional del usuario `userDetails` por cada item.
                    """,
                            tags = {"Solicitud - listar"},
                            security = { @SecurityRequirement(name = "bearerAuth") },
                            parameters = {
                                    @Parameter(
                                            name = "correoElectronico",
                                            description = "Filtra por correo del solicitante (coincidencia exacta, sin espacios).",
                                            in = ParameterIn.QUERY,
                                            required = false,
                                            schema = @Schema(type = "string", example = "cliente@dominio.com")
                                    ),
                                    @Parameter(
                                            name = "tipoPrestamoId",
                                            description = "Filtra por ID del tipo de préstamo.",
                                            in = ParameterIn.QUERY,
                                            required = false,
                                            schema = @Schema(type = "integer", format = "int64", example = "2")
                                    ),
                                    @Parameter(
                                            name = "estadoId",
                                            description = "Filtra por ID del estado de la solicitud.",
                                            in = ParameterIn.QUERY,
                                            required = false,
                                            schema = @Schema(type = "integer", format = "int64", example = "1")
                                    ),
                                    @Parameter(
                                            name = "page",
                                            description = "Índice de página (base 0).",
                                            in = ParameterIn.QUERY,
                                            required = false,
                                            schema = @Schema(type = "integer", example = "0", defaultValue = "0", minimum = "0")
                                    ),
                                    @Parameter(
                                            name = "size",
                                            description = "Tamaño de página.",
                                            in = ParameterIn.QUERY,
                                            required = false,
                                            schema = @Schema(type = "integer", example = "10", defaultValue = "10", minimum = "1", maximum = "100")
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Listado recuperado correctamente.",
                                            content = @Content(
                                                    mediaType = "application/json",
                                                    schema = @Schema(
                                                            implementation = Object.class,
                                                            description = "Modelo paginado con metadatos y lista de `SolicitudDto`."
                                                    ),
                                                    examples = {
                                                            @ExampleObject(
                                                                    name = "Página con resultados",
                                                                    value = """
                                                                        {
                                                                          "content": [
                                                                            {
                                                                              "solicitudId": 101,
                                                                              "monto": 15000000.0,
                                                                              "plazo": 24,
                                                                              "correoElectronico": "cliente@dominio.com",
                                                                              "tipoPrestamoId": 2,
                                                                              "estadoId": 1,
                                                                              "usuarioId": 55,
                                                                              "user": {
                                                                                "id": 55,
                                                                                "nombres": "Ana",
                                                                                "apellidos": "Pérez",
                                                                                "correoElectronico": "cliente@dominio.com"
                                                                              }
                                                                            }
                                                                          ],
                                                                          "page": 0,
                                                                          "size": 10,
                                                                          "totalElements": 1,
                                                                          "totalPages": 1
                                                                        }
                                                                    """
                                                            ),
                                                            @ExampleObject(
                                                                    name = "Página vacía",
                                                                    value = """
                                                                        {
                                                                          "content": [],
                                                                          "page": 0,
                                                                          "size": 10,
                                                                          "totalElements": 0,
                                                                          "totalPages": 0
                                                                        }
                                                                    """
                                                            )
                                                    }
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
                                            description = "No autorizado (solo asesores pueden listar).",
                                            content = @Content(mediaType = "application/json",
                                                    examples = @ExampleObject(value = """
                                                            { "message": "Solo los asesores pueden listar solicitudes." }
                                                        """
                                                    )
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Parámetros de consulta inválidos (validación).",
                                            content = @Content(mediaType = "application/json",
                                                    examples = @ExampleObject(value = """
                                                            {
                                                              "message": "Parámetros inválidos",
                                                              "errors": {
                                                                "page": "Debe ser >= 0",
                                                                "size": "Debe estar entre 1 y 100"
                                                              }
                                                            }
                                                        """
                                                    )
                                            )
                                    )
                            }
                    )
            )
    })
    @Bean public RouterFunction<ServerResponse> routerFunction() {
        return route()
                .POST(solicitudPath.getSolicitud(), handler::createSolicitud)
                .GET(solicitudPath.getSolicitud(), handler::listAll)
                .PUT(solicitudPath.getSolicitud() + "/{id}", handler::updateEstadoSolicitud)
                .build();
    }
}
