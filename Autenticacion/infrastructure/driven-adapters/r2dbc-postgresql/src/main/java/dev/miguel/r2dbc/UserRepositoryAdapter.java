package dev.miguel.r2dbc;

import dev.miguel.model.user.Token;
import dev.miguel.model.user.User;
import dev.miguel.model.user.gateways.UserRepository;
import dev.miguel.r2dbc.entity.UserEntity;
import dev.miguel.r2dbc.helper.ReactiveAdapterOperations;
import dev.miguel.r2dbc.jwt.provider.JwtProvider;
import lombok.extern.log4j.Log4j2;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Repository
@Log4j2
public class UserRepositoryAdapter extends ReactiveAdapterOperations<
        User,
        UserEntity,
        Long,
        UserReactiveRepository
> implements UserRepository {

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public UserRepositoryAdapter(UserReactiveRepository repository, ObjectMapper mapper, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        super(repository, mapper, entity -> mapper.map(entity, User.class));
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public Mono<User> saveUser(User user) {

        return Mono.fromCallable(() -> {
                    log.debug("Codificando contraseña para usuario con correo: {}", user.getCorreoElectronico());
                    return passwordEncoder.encode(user.getContrasenia());
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(encoded -> {
                    log.debug("Contraseña codificada");
                    return user.toBuilder().contrasenia(encoded).build();
                })
                .flatMap(u -> {
                    log.debug("Persistiendo usuario en la base de datos");
                    return super.save(u);
                })
                .doOnSuccess(saved -> log.info("Usuario creado con id = {}", saved.getId()))
                .doOnError(e -> log.error("Error al guardar usuario: {}", user, e));
    }



    @Override
    public Mono<User> findUserById(Long id) {
        log.info("Buscando usuario por id={}", id);

        return super.findById(id)
                .doOnNext(user -> log.info("Usuario encontrado: {}", user))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("No se encontró usuario con id = {}", id);
                    return Mono.empty();
                }))
                .doOnError(error -> log.error("Error al buscar usuario por id = {}", id, error));
    }

    @Override
    public Mono<User> findUserByEmail(String email) {
        log.info("Buscando usuario por email={}", email);

        User user = new User();
        user.setCorreoElectronico(email);

        return super.findByExample(user)
                .next()
                .doOnNext(found -> log.info("Usuario encontrado con email = {}: {}", email, found))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("No se encontró usuario con email = {}", email);
                    return Mono.empty();
                }))
                .doOnError(error -> log.error("Error al buscar usuario por email = {}", email, error));
    }


    @Override
    public Mono<Token> login(User user, String rawPassword) {
        log.info("Validando contraseña para userId={} email={}", user.getId(), user.getCorreoElectronico());

        return Mono.fromCallable(() -> passwordEncoder.matches(rawPassword, user.getContrasenia()))
                .subscribeOn(Schedulers.boundedElastic()) // BCrypt fuera del event loop
                .doOnNext(matches -> {
                    if (!matches) {
                        log.warn("Credenciales inválidas para userId = {}", user.getId());
                    } else {
                        log.info("Password OK para userId = {}", user.getId());
                    }
                })
                .filter(Boolean::booleanValue)
                .flatMap(ignored ->
                        jwtProvider.generateToken(user) // Mono<String>
                                .doOnSubscribe(s -> log.info("Generando JWT para userId = {} rolId = {}", user.getId(), user.getRolId()))
                                .doOnNext(jwt -> log.info("JWT generado para userId = {}", user.getId()))
                )
                .map(Token::new)
                .doOnError(e -> log.error("Error en login para userId={}", user.getId(), e));
    }


}
