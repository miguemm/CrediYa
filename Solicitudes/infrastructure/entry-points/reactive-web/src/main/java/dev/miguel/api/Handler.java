package dev.miguel.api;

import dev.miguel.api.DTO.CreateSolicitudDTO;
import dev.miguel.api.mapper.SolicitudDtoMapper;
import dev.miguel.model.userContext.UserContext;
import dev.miguel.usecase.solicitud.gateways.ISolicitudUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.security.Principal;

@Component
@RequiredArgsConstructor
@Log4j2
public class Handler {

    private final ISolicitudUseCase solicitudUseCase;
    private final SolicitudDtoMapper solicitudDtoMapper;

    public Mono<ServerResponse> createSolicitud(ServerRequest req) {
        log.info("--- Petición recibida en createSolicitud ---");

        return Mono.zip(
                        req.principal().map(this::toUserContext),              // Mono<UserContext>
                        req.bodyToMono(CreateSolicitudDTO.class)               // Mono<CreateSolicitudDTO>
                )
                .doOnNext(t -> log.info("DTO recibido: {}", t.getT2()))
                .map(t -> solicitudDtoMapper.toDomain(t.getT2()))        // Solicitud
                .zipWith(req.principal().map(this::toUserContext))       // (Solicitud, UserContext)
                .doOnNext(t -> log.info("USER RECIBIDO: {}", t.getT2()))
                .flatMap(t -> solicitudUseCase.createSolicitud(t.getT1(), t.getT2()))
                .doOnNext(s -> log.info("Entidad creada: {}", s))
                .flatMap(s -> ServerResponse
                        .created(URI.create("/api/v1/solicitud/"))
                        .build()
                )
                .doOnError(e -> log.error("Error en createSolicitud", e));
    }

    private UserContext toUserContext(Principal p) {
        if (p instanceof JwtAuthenticationToken jat) {
            var jwt   = jat.getToken(); // org.springframework.security.oauth2.jwt.Jwt
            var id    = jwt.getSubject(); // "sub"
            var email = jwt.getClaimAsString("email"); // si lo incluyes
            var roles = jat.getAuthorities().stream()
                    .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                    .toList();
            return new UserContext(id, email, roles);
        }
        if (p instanceof org.springframework.security.core.Authentication a) {
            var roles = a.getAuthorities().stream()
                    .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                    .toList();
            return new UserContext(a.getName(), null, roles);
        }
        return new UserContext(p.getName(), null, java.util.List.of());
    }
}
