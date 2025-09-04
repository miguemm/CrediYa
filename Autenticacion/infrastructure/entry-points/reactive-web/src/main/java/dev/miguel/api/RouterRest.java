package dev.miguel.api;

import dev.miguel.api.DTO.ApiErrorResponse;
import dev.miguel.api.DTO.CreateUserDTO;
import dev.miguel.api.DTO.LogInDTO;
import dev.miguel.api.config.AppPaths;
import dev.miguel.model.user.Token;
import dev.miguel.model.utils.userContext.UserDetails;
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

    private final AppPaths appPaths;
    private final UserHandler userHandler;

    @RouterOperations({
            // =========================
            // POST /api/v1/usuario  (protegido: asesor, administrador)
            // =========================
            @RouterOperation(
                    path = "/api/v1/usuario",
                    produces = { MediaType.APPLICATION_JSON_VALUE },
                    method = RequestMethod.POST,
                    beanClass = UserHandler.class,
                    beanMethod = "createUser",
                    operation = @Operation(
                            operationId = "createUser",
                            summary = "Crear usuario",
                            description = """
                                Crea un nuevo usuario en el sistema. Requiere autenticación con rol **asesor** o **administrador**.
                                Valida unicidad de correo y existencia del rol; cifra la contraseña antes de persistir.
                            """,
                            tags = {"Autenticacion - Usuarios"},
                             security = { @SecurityRequirement(name = "bearerAuth") },
                            requestBody = @RequestBody(
                                    required = true,
                                    description = "Datos necesarios para crear el usuario.",
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = CreateUserDTO.class),
                                            examples = @ExampleObject(
                                                    name = "Ejemplo válido",
                                                    value = """
                                                        {
                                                          "nombres": "Ana",
                                                          "apellidos": "Pérez",
                                                          "fechaNacimiento": "1995-08-20",
                                                          "direccion": "Calle 123",
                                                          "telefono": "3001234567",
                                                          "correoElectronico": "ana.perez@dominio.com",
                                                          "contrasenia": "MiPass$egura123",
                                                          "salarioBase": 4500000.00,
                                                          "rolId": 2
                                                        }
                                                    """
                                            )
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "201",
                                            description = "Usuario creado correctamente.",
                                            content = @Content,
                                            headers = @Header(
                                                    name = "Location",
                                                    description = "URI del usuario creado.",
                                                    schema = @Schema(type = "string", example = "/api/v1/usuario/123")
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Solicitud inválida (formato/body incorrecto).",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                                    examples = @ExampleObject(value = """
                                                            {
                                                              "message": "Datos inválidos",
                                                              "errors": { "correoElectronico": "Formato de email inválido" }
                                                            }
                                                        """
                                                    )
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "401",
                                            description = "No autenticado.",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                                    examples = @ExampleObject(value = """
                                                                { "message": "No autenticado" }
                                                            """
                                                    )
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "403",
                                            description = "No autorizado (rol insuficiente).",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                                    examples = @ExampleObject(value = """
                                                                { "message": "Acceso denegado" }
                                                            """
                                                    )
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Conflicto: el correo ya existe.",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                                    examples = @ExampleObject(value = """
                                                                { "message": "El correo ya está registrado" }
                                                            """
                                                    )
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Validación de negocio: rol no existe u otras reglas.",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                                    examples = @ExampleObject(value = """
                                                                { "message": "El rol especificado no existe" }
                                                            """
                                                    )
                                            )
                                    )
                            }
                    )
            ),

            // =========================
            // GET /api/v1/usuario/{id}  (protegido: asesor)
            // =========================
            @RouterOperation(
                    path = "/api/v1/usuario/{id}",
                    produces = { MediaType.APPLICATION_JSON_VALUE },
                    method = RequestMethod.GET,
                    beanClass = UserHandler.class,
                    beanMethod = "getUserById",
                    operation = @Operation(
                            operationId = "getUserById",
                            summary = "Obtener usuario por ID",
                            description = """
                                Retorna los detalles de un usuario por su identificador. Requiere autenticación con rol **asesor**.
                            """,
                            tags = {"Autenticacion - Usuarios"},
                             security = { @SecurityRequirement(name = "bearerAuth") },
                            parameters = {
                                    @Parameter(
                                            name = "id",
                                            description = "Identificador del usuario.",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            schema = @Schema(type = "integer", format = "int64", example = "123")
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Usuario encontrado.",
                                            content = @Content(
                                                    mediaType = "application/json",
                                                    schema = @Schema(implementation = UserDetails.class),
                                                    examples = @ExampleObject(value = """
                                                            {
                                                              "id": 123,
                                                              "nombres": "Ana",
                                                              "apellidos": "Pérez",
                                                              "fechaNacimiento": "1995-08-20",
                                                              "direccion": "Calle 123",
                                                              "telefono": "3001234567",
                                                              "correoElectronico": "ana.perez@dominio.com",
                                                              "salarioBase": 4500000.00,
                                                              "rolId": 2
                                                            }
                                                        """
                                                    )
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "401",
                                            description = "No autenticado.",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ApiErrorResponse.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "403",
                                            description = "No autorizado (rol insuficiente).",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ApiErrorResponse.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "Usuario no encontrado.",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                                    examples = @ExampleObject(value = """
                                                            { "message": "Usuario no existe" }
                                                            """
                                                    )
                                            )
                                    )
                            }
                    )
            ),

            // =========================
            // POST /api/v1/auth/login  (público)
            // =========================
            @RouterOperation(
                    path = "/api/v1/auth/login",
                    produces = { MediaType.APPLICATION_JSON_VALUE },
                    method = RequestMethod.POST,
                    beanClass = UserHandler.class,
                    beanMethod = "logIn",
                    operation = @Operation(
                            operationId = "logIn",
                            summary = "Iniciar sesión (login)",
                            description = """
                            Autentica a un usuario por correo y contraseña. Retorna un token (p. ej. JWT) para usar en llamadas subsecuentes.
                            """,
                            tags = {"Autenticacion - Usuarios"},
                            requestBody = @RequestBody(
                                    required = true,
                                    description = "Credenciales de acceso.",
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = LogInDTO.class),
                                            examples = @ExampleObject(value = """
                                                    { "correoElectronico": "ana.perez@dominio.com", "contrasenia": "MiPass$egura123" }
                                                """
                                            )
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Autenticación exitosa.",
                                            content = @Content(
                                                    mediaType = "application/json",
                                                    schema = @Schema(implementation = Token.class),
                                                    examples = @ExampleObject(value = """
                                                            { "accessToken": "eyJhbGciOi...", "tokenType": "Bearer", "expiresIn": 3600 }
                                                        """
                                                    )
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "401",
                                            description = "Credenciales inválidas o usuario no existe.",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                                    examples = @ExampleObject(value = """
                                                                { "message": "Correo o contraseña incorrectos" }
                                                            """
                                                    )
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "401",
                                            description = "Datos de login inválidos (validación).",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ApiErrorResponse.class))
                                    )
                            }
                    )
            )
    })
    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return route()
                .POST(appPaths.getUsuario(), userHandler::createUser)
                .GET(appPaths.getUsuario() + "/{id}", userHandler::getUserById)
                .POST(appPaths.getAuthentication(), userHandler::logIn)
                .build();
    }
}
