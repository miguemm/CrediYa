package dev.miguel.authenticationwebclient;

import dev.miguel.model.solicitud.proyections.SolicitudDto;
import dev.miguel.model.utils.exception.BusinessException;
import dev.miguel.model.utils.userContext.UserDetails;
import dev.miguel.model.utils.userContext.gateways.IGetUserDetailsById;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@Log4j2
@AllArgsConstructor
public class getUserDetailsProvider implements IGetUserDetailsById {

    private final WebClient userWebClient;

    @Override
    public Mono<UserDetails> getUserDetailsById(SolicitudDto solicitud) {
        return Mono.defer(() -> {
            Long userId = solicitud.getUsuarioId();
            log.info("[getUserDetailsById] inicio solicitudId={} usuarioId={}",
                    solicitud.getSolicitudId(), userId);

            if (userId == null) {
                log.warn("[getUserDetailsById] usuarioId es null para solicitudId={}", solicitud.getSolicitudId());
                return Mono.error(new BusinessException("usuarioId es requerido"));
            }

            return userWebClient.get()
                    .uri(uriBuilder -> {
                        var uri = uriBuilder.path("/{id}").build(userId);
                        log.debug("[WebClient] GET {}", uri);
                        return uri;
                    })
                    .accept(MediaType.APPLICATION_JSON)
                    .headers(h -> {
                        boolean hasAuth = h.containsKey(HttpHeaders.AUTHORIZATION);
                        log.debug("[WebClient] headers -> Authorization presente: {}", hasAuth);
                    })
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, resp ->
                            resp.bodyToMono(String.class)
                                    .doOnNext(body -> log.warn("[WebClient] 4xx id={} status={} body={}",
                                            userId, resp.statusCode(), body))
                                    .defaultIfEmpty("Cliente inválido al consultar usuario")
                                    .map(msg -> new BusinessException("4xx al consultar usuario " + userId + ": " + msg))
                    )
                    .onStatus(HttpStatusCode::is5xxServerError, resp ->
                            resp.bodyToMono(String.class)
                                    .doOnNext(body -> log.error("[WebClient] 5xx id={} status={} body={}",
                                            userId, resp.statusCode(), body))
                                    .defaultIfEmpty("Error del servidor de usuarios")
                                    .map(msg -> new BusinessException("5xx al consultar usuario " + userId + ": " + msg))
                    )
                    .bodyToMono(UserDetails.class)
                    .doOnNext(u -> log.info("[getUserDetailsById] OK id={} user={}", userId, u))
                    .timeout(Duration.ofSeconds(4))
                    .retryWhen(
                            Retry.backoff(2, Duration.ofMillis(200))
                                    .filter(ex ->
                                            ex instanceof java.util.concurrent.TimeoutException
                                                    || (ex instanceof WebClientResponseException we
                                                    && we.getStatusCode().is5xxServerError())
                                    )
                                    .doBeforeRetry(sig -> log.warn("[Retry] intento #{} por {} (id={})",
                                            sig.totalRetries() + 1, sig.failure(), userId))
                    )
                    .doOnError(e -> log.error("[getUserDetailsById] error id={} -> {}", userId, e.toString(), e))
                    .doFinally(signal -> log.info("[getUserDetailsById] fin id={} signal={}", userId, signal));
        });
    }

}
