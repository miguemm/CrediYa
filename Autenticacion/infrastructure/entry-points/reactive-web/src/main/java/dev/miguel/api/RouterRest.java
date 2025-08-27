package dev.miguel.api;

import dev.miguel.api.config.UsuarioPath;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final UsuarioPath usuarioPath;
    private final Handler handler;

    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return route(POST(usuarioPath.getUsuario()), handler::createUser)
                .andRoute(GET(usuarioPath.getUsuarioById()), handler::getUserById);
    }
}
