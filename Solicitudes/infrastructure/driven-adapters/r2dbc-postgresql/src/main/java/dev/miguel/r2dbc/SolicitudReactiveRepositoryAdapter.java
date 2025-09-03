package dev.miguel.r2dbc;

import dev.miguel.model.solicitud.Solicitud;
import dev.miguel.model.solicitud.gateways.SolicitudRepository;
import dev.miguel.model.solicitud.proyections.findAllSolicitudes;
import dev.miguel.model.utils.page.PageModel;
import dev.miguel.r2dbc.entity.SolicitudEntity;
import dev.miguel.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.log4j.Log4j2;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

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

    @Override
    public Mono<PageModel<findAllSolicitudes>> findAll(String correo, Long tipoPrestamoId, Long estadoId, int page, int size) {
        long offset = (long) page * size;

        Mono<List<findAllSolicitudes>> rows = repository.findAllProjected(estadoId, correo, tipoPrestamoId, size, offset).collectList();
        Mono<Long> total = repository.countAllProjected(estadoId, correo, tipoPrestamoId);

        return Mono.zip(rows, total)
                .map(t -> {
                    var content = t.getT1();
                    long totalElements = t.getT2();
                    int totalPages = (int) ((totalElements + size - 1) / size);
                    return PageModel.<findAllSolicitudes>builder()
                            .content(content)
                            .page(page)
                            .size(size)
                            .totalElements(totalElements)
                            .totalPages(totalPages)
                            .hasNext(page + 1 < totalPages)
                            .hasPrevious(page > 0)
                            .build();
                });
    }
}
