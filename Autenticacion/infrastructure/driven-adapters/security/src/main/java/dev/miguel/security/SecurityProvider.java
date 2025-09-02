package dev.miguel.security;

import dev.miguel.model.user.Token;
import dev.miguel.model.user.User;
import dev.miguel.model.user.gateways.ISecurityProvider;
import dev.miguel.security.jwt.provider.JwtProvider;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Log4j2
public class SecurityProvider implements ISecurityProvider {

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public SecurityProvider(PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public Mono<String> encryptPassword(User user) {
        return Mono.fromCallable(() -> {
            log.debug("Codificando contraseña para usuario con correo: {}", user.getCorreoElectronico());
            return passwordEncoder.encode(user.getContrasenia());
        });
    }

    @Override
    public Mono<Boolean> validatePassword(User user, String password) {
        log.info("Validando contraseña para userId = {} email = {}", user.getId(), user.getCorreoElectronico());

        return Mono.fromCallable(() -> passwordEncoder.matches(password, user.getContrasenia()))
                .subscribeOn(Schedulers.boundedElastic()) // BCrypt fuera del event loop
                .doOnNext(matches -> {
                    if (!matches) {
                        log.warn("Credenciales inválidas para userId = {}", user.getId());
                    } else {
                        log.info("Password OK para userId = {}", user.getId());
                    }
                });
    }

    @Override
    public Mono<Token> generateToken(User user) {
        return jwtProvider.generateToken(user) // Mono<String> (JWT)
                .doOnSubscribe(s -> log.info("Generando JWT para userId={} rolId={}", user.getId(), user.getRolId()))
                .map(Token::new)
                .doOnSuccess(t -> log.info("JWT generado para userId={}", user.getId()));
    }


}
