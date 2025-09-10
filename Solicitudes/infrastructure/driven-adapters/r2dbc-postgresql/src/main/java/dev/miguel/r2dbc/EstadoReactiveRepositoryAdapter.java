package dev.miguel.r2dbc;

import dev.miguel.model.estado.Estado;
import dev.miguel.model.estado.gateways.EstadoRepository;
import dev.miguel.r2dbc.entity.EstadoEntity;
import dev.miguel.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.log4j.Log4j2;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@Log4j2
public class EstadoReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Estado,
        EstadoEntity,
        Long,
        EstadoReactiveRepository
> implements EstadoRepository {
    public EstadoReactiveRepositoryAdapter(EstadoReactiveRepository repository, ObjectMapper mapper) {

        super(repository, mapper, entity -> mapper.map(entity, Estado.class));
    }

    @Override
    public Mono<Boolean> existsEstadoById(Long id) {
        log.info("Verificando existencia de estado id={}", id);

        return super.findById(id)       // Mono<Estado>
                .hasElement()               // Mono<Boolean>
                .doOnSuccess(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        log.info("Existe estado id={}", id);
                    } else {
                        log.warn("No existe estado id={}", id);
                    }
                })
                .doOnError(e -> log.error("Error verificando estado id={}", id, e));
    }

    @Override
    public Mono<Estado> findEstadoById(Long id) {
        log.info("Find estado by id: {}", id);

        return super.findById(id)
                .doOnNext(saved -> log.info("Encontrad estado con id = {}", saved.getId()))
                .doOnError(error -> log.error("Error al obtener estado: {}", id, error));
    }

}
