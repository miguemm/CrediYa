package dev.miguel.api;

import dev.miguel.api.DTO.CreateUserDTO;
import dev.miguel.api.DTO.UserDTO;
import dev.miguel.api.config.UsuarioPath;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final UsuarioPath usuarioPath;
    private final Handler handler;

    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/usuario",
                    produces = { MediaType.APPLICATION_JSON_VALUE },
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "createUser",
                    operation = @Operation(
                            operationId = "createUser",
                            summary = "Crear usuario",
                            description = "Crea un nuevo usuario en el sistema",
                            tags = {"Autenticacion - Usuarios"},
//                            security = { @SecurityRequirement(name = "bearerAuth") }, // listo para JWT a futuro
                            requestBody = @RequestBody(
                                    required = true,
                                    description = "Datos necesarios para crear el usuario",
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = CreateUserDTO.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Usuario creado correctamente",
                                            content = @Content(
                                                    mediaType = "application/json",
                                                    schema = @Schema(implementation = UserDTO.class)
                                            )
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/usuario/{id}",
                    produces = { MediaType.APPLICATION_JSON_VALUE },
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "getUserById",
                    operation = @Operation(
                            operationId = "getUserById",
                            summary = "Obtener usuario por ID",
                            description = "Devuelve un usuario existente por su identificador",
                            tags = {"Autenticacion - Usuarios"},
//                            security = { @SecurityRequirement(name = "bearerAuth") }, // listo para JWT a futuro
                            parameters = {
                                    @Parameter(
                                            in = ParameterIn.PATH,
                                            name = "id",
                                            required = true,
                                            description = "ID del usuario",
                                            schema = @Schema(implementation = Long.class)
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Usuario encontrado",
                                            content = @Content(
                                                    mediaType = "application/json",
                                                    schema = @Schema(implementation = UserDTO.class)
                                            )
                                    ),
                                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
                            }
                    )
            )
    })
    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return route(POST(usuarioPath.getUsuario()), handler::createUser)
                .andRoute(GET(usuarioPath.getUsuarioById()), handler::getUserById);
    }
}
