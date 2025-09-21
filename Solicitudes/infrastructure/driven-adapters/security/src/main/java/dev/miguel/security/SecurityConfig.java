package dev.miguel.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
public class SecurityConfig {

    private static final String[] WHITE_LIST_URL = {
            "/api/v1/solicitud",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/webjars/swagger-ui/**",
            "/actuator/**"
    };

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            ReactiveJwtDecoder jwtDecoder
    ) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers(WHITE_LIST_URL).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtDecoder(jwtDecoder)
                                .jwtAuthenticationConverter(this::convert)
                        )
                )
                .build();
    }

    private Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        var roles = jwt.getClaimAsStringList("roles");
        var authorities = (roles == null ? List.<GrantedAuthority>of()
                : roles.stream().map(SimpleGrantedAuthority::new).toList());

        String principalName = jwt.getClaimAsString("sub");

        return Mono.just(new JwtAuthenticationToken(jwt, authorities, principalName));
    }
}
