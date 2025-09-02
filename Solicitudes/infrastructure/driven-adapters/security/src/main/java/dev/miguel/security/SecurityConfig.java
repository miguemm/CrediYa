package dev.miguel.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            ReactiveJwtDecoder jwtDecoder
    ) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        // Rutas abiertas
                        .pathMatchers(HttpMethod.POST, "/api/v1/solicitud").permitAll()
                        .pathMatchers("/public/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        // Todo lo demás requiere JWT
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtDecoder(jwtDecoder)
                                .jwtAuthenticationConverter(this::convert) // <- OJO
                        )
                )
                .build();
    }

    private Mono<AbstractAuthenticationToken> convert(Jwt jwt) { // <- tipo correcto
        var roles = jwt.getClaimAsStringList("roles"); // ajusta al claim que uses
        var authorities = (roles == null ? List.<GrantedAuthority>of()
                : roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList());

        String principalName = jwt.getClaimAsString("sub"); // o "preferred_username", etc.

        return Mono.just(new JwtAuthenticationToken(jwt, authorities, principalName));
    }
}
