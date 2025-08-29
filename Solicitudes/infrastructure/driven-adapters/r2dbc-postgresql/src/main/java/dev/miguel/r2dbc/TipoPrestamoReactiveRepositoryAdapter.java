package dev.miguel.r2dbc;

import dev.miguel.model.tipoprestamo.TipoPrestamo;
import dev.miguel.model.tipoprestamo.gateways.TipoPrestamoRepository;
import dev.miguel.r2dbc.entity.TipoPrestamoEntity;
import dev.miguel.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.log4j.Log4j2;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@Log4j2
public class TipoPrestamoReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        TipoPrestamo,
        TipoPrestamoEntity,
        Long,
        TipoPrestamoReactiveRepository
> implements TipoPrestamoRepository {
    public TipoPrestamoReactiveRepositoryAdapter(TipoPrestamoReactiveRepository repository, ObjectMapper mapper) {

        super(repository, mapper, entity -> mapper.map(entity, TipoPrestamo.class));
    }

    @Override
    public Mono<Boolean> existsTipoPrestamoById(Long id) {
        log.info("Verificando existencia de tipo prestamo id={}", id);

        return super.findById(id)
                .hasElement()
                .doOnSuccess(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        log.info("Existe tipo prestamo id={}", id);
                    } else {
                        log.warn("No existe tipo prestamo id={}", id);
                    }
                })
                .doOnError(e -> log.error("Error verificando tipo prestamo id={}", id, e));
    }


}
