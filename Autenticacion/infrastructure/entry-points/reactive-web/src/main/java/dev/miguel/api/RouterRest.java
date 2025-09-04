package dev.miguel.api;

import dev.miguel.api.DTO.ApiErrorResponse;
import dev.miguel.api.DTO.CreateUserDTO;
import dev.miguel.api.config.AppPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
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

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final AppPaths appPaths;
    private final UserHandler userHandler;

    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/usuario",
                    produces = { MediaType.APPLICATION_JSON_VALUE },
                    method = RequestMethod.POST,
                    beanClass = UserHandler.class,
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
                                            responseCode = "201",
                                            description = "Usuario creado correctamente",
                                            content = @Content,
                                            headers = {
                                                    @Header(
                                                            name = "Location",
                                                            description = "URI del usuario creado",
                                                            schema = @Schema(type = "string")
                                                    )
                                            }
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Solicitud inválida",
                                            content = @Content(
                                                    mediaType = "application/json",
                                                    schema = @Schema(implementation = ApiErrorResponse.class)
                                            )
                                    ),
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
