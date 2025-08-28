package dev.miguel.r2dbc;

import dev.miguel.model.solicitud.Solicitud;
import dev.miguel.model.solicitud.gateways.SolicitudRepository;
import dev.miguel.r2dbc.entity.SolicitudEntity;
import dev.miguel.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.log4j.Log4j2;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@Log4j2
public class SolicitudReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Solicitud,
        SolicitudEntity,
        Long,
        SolicitudReactiveRepository
> implements SolicitudRepository {
    public SolicitudReactiveRepositoryAdapter(SolicitudReactiveRepository repository, ObjectMapper mapper) {

        super(repository, mapper, entity -> mapper.map(entity, Solicitud.class));
    }

    @Override
    public Mono<Solicitud> saveSolicitud(Solicitud solicitud) {
        log.info("Guardando usuario: {}", solicitud);

        return super.save(solicitud)
                .doOnNext(saved -> log.info("Solicitud creada con id = {}", saved.getId()))
                .doOnError(error -> log.error("Error al guardar solicitud: {}", solicitud, error));
    }
}
