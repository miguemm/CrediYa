package dev.miguel.r2dbc;

import dev.miguel.model.rol.Rol;
import dev.miguel.model.rol.gateways.RolRepository;
import dev.miguel.r2dbc.entity.RolEntity;
import dev.miguel.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.log4j.Log4j2;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@Log4j2
public class RolRepositoryAdapter extends ReactiveAdapterOperations<
        Rol,
        RolEntity,
        Long,
        RolReactiveRepository
> implements RolRepository {

    public RolRepositoryAdapter(RolReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, entity -> mapper.map(entity, Rol.class));
    }

    @Override
    public Mono<Boolean> existsById(Long id) {
        return super.findById(id)
                .hasElement()
                .doOnSuccess(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        log.info("Existe rol id={}", id);
                    } else {
                        log.warn("No existe rol id={}", id);
                    }
                })
                .doOnError(e -> log.error("Error verificando rol id={}", id, e));
    }
}
