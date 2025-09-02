package dev.miguel.r2dbc;

import dev.miguel.model.user.User;
import dev.miguel.model.user.gateways.UserRepository;
import dev.miguel.r2dbc.entity.UserEntity;
import dev.miguel.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.log4j.Log4j2;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@Log4j2
public class UserRepositoryAdapter extends ReactiveAdapterOperations<
        User,
        UserEntity,
        Long,
        UserReactiveRepository
> implements UserRepository {


    public UserRepositoryAdapter(UserReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, entity -> mapper.map(entity, User.class));
    }

    @Override
    public Mono<User> saveUser(User user) {
        log.debug("Persistiendo usuario en la base de datos");

        return super.save(user)
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

}
