package dev.miguel.security.jwt;

import dev.miguel.model.utils.exceptions.UnauthorizedException;
import dev.miguel.model.utils.userContext.UserContext;
import dev.miguel.model.utils.userContext.gateways.IExtractUserContext;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class JwtService implements IExtractUserContext {

    @Override
    public Mono<UserContext> toUserContext(Mono<?> p) {
        Mono<?> safe = (p == null) ? Mono.empty() : p;

        return safe
                .switchIfEmpty(Mono.error(new UnauthorizedException("No autenticado")))
                .flatMap(this::validateUserContext); // reutiliza la versión que recibe Object
    }

    private Mono<UserContext> validateUserContext(Object p) {
        if (p instanceof JwtAuthenticationToken jat) {
            var jwt = jat.getToken();
            if (jwt == null) {
                return Mono.error(new UnauthorizedException("Token inválido"));
            }

            var id    = jwt.getSubject();
            var email = jwt.getClaimAsString("email");
            var roles = jat.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(r -> r != null && !r.isBlank())
                    .toList();

            if (isBlank(id) || isBlank(email) || roles.isEmpty()) {
                return Mono.error(new UnauthorizedException("Token incompleto"));
            }

            return Mono.just(new UserContext(id, email, roles));
        }

        return Mono.error(new UnauthorizedException("Tipo de autenticación no soportado"));
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
