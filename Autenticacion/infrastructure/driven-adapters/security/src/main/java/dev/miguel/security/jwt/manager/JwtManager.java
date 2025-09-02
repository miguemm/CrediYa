package dev.miguel.security.jwt.manager;

import dev.miguel.security.jwt.provider.JwtProvider;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtManager implements ReactiveAuthenticationManager {

    private final JwtProvider jwtProvider;

    public JwtManager(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.just(authentication)
                .map(auth -> jwtProvider.getClaims(auth.getCredentials().toString()))
                .onErrorResume(e -> Mono.error(new Throwable("bad token")))
                .map(claims -> {
                    Object rolesObj = claims.get("roles");

                    List<SimpleGrantedAuthority> authorities;
                    if (rolesObj instanceof List<?> list) {

                        authorities = list.stream()
                                .map(Object::toString) // evitamos cast directo
                                .map(this::normalizeRole)
                                .map(SimpleGrantedAuthority::new)
                                .toList();
                    } else if (rolesObj instanceof String s) {

                        authorities = List.of(new SimpleGrantedAuthority(normalizeRole(s)));
                    } else {
                        throw new IllegalArgumentException("Claim 'roles' debe ser List<String> o String");
                    }

                    return new UsernamePasswordAuthenticationToken(
                            claims.getSubject(),
                            null,
                            authorities
                    );
                });
    }

    private String normalizeRole(String role) {
        return role != null && role.startsWith("ROLE_") ? role : "ROLE_" + role;
    }

}