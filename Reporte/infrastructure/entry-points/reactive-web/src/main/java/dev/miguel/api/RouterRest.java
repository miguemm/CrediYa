package dev.miguel.api;

import dev.miguel.api.config.ReportesPath;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final ReportesPath reportesPath;
    private final Handler handler;

    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return route()
                .GET(reportesPath.getReportes(), handler::consultarMetricas)
                .build();
    }
}
