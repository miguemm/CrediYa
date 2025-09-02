package dev.miguel.security.jwt.filter;

import dev.miguel.model.exception.BusinessException;
import dev.miguel.model.exception.SecurityException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtFilter implements WebFilter {

    private static final String[] WHITE_LIST_URL = {
            "/api/v1/auth/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/webjars/swagger-ui/**"
    };

    private final AntPathMatcher matcher = new AntPathMatcher();

    private boolean isWhitelisted(String path) {
        for (String pattern : WHITE_LIST_URL) {
            if (matcher.match(pattern, path)) return true;
        }
        return false;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }
        
        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Falta autenticación"));
        }

        String token = auth.substring(7);
        exchange.getAttributes().put("token", token);
        return chain.filter(exchange);
    }

}