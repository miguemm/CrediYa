package dev.miguel.authenticationwebclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient userWebClient(WebClient.Builder builder, @Value("${webClient.microAutenticacion}") String microAutenticacion) {
        return builder
                .baseUrl(microAutenticacion)
                .filter((request, next) ->
                        Mono.deferContextual(ctx -> {
                            ClientRequest.Builder b = ClientRequest.from(request);

                            ServerWebExchange exchange = ctx.getOrDefault(ServerWebExchange.class, null);
                            if (exchange != null) {
                                String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                                if (auth != null && !auth.isBlank()) {
                                    b.header(HttpHeaders.AUTHORIZATION, auth);
                                }
                            }
                            return next.exchange(b.build());
                        })
                )
                .build();
    }
}
