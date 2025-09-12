package dev.miguel.r2dbc;

import dev.miguel.model.solicitud.Solicitud;
import dev.miguel.model.solicitud.gateways.SolicitudRepository;
import dev.miguel.model.solicitud.proyections.SolicitudDto;
import dev.miguel.model.utils.page.PageModel;
import dev.miguel.r2dbc.entity.SolicitudEntity;
import dev.miguel.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.log4j.Log4j2;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
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

    private final TransactionalOperator tx;

    public SolicitudReactiveRepositoryAdapter(SolicitudReactiveRepository repository, ObjectMapper mapper, TransactionalOperator tx) {
        super(repository, mapper, entity -> mapper.map(entity, Solicitud.class));
        this.tx = tx;
    }

    @Override
    public Mono<Solicitud> saveSolicitud(Solicitud solicitud) {
        log.info("Guardando solicitud: {}", solicitud);

        return super.save(solicitud)
                .doOnNext(saved -> log.info("Solicitud creada con id = {}", saved.getId()))
                .doOnError(error -> log.error("Error al guardar solicitud: {}", solicitud, error))
                .as(tx::transactional);
    }

    @Override
    public Mono<Solicitud> findSolicitudById(Long solicitudId) {
        log.info("Find solicitud by id: {}", solicitudId);

        return super.findById(solicitudId)
                .doOnNext(saved -> log.info("Encontrada solicitud con id = {}", saved.getId()))
                .doOnError(error -> log.error("Error al obtener solicitud: {}", solicitudId, error));
    }

    @Override
    public Flux<SolicitudDto> findAllSolicitudesAprobadasByUsuarioId(Long userId) {
        log.info("Find all solicitudes by userId: {}", userId);

        return repository.findAllSolicitudesAprobadasByUsuarioId(userId)
                .doOnComplete(() -> log.info("Fetched all solicitudes for userId={}", userId))
                .doOnError(err -> log.error("Error fetching solicitudes for userId={}", userId, err));
    }

    @Override
    public Mono<PageModel<SolicitudDto>> findAll(String correo, Long tipoPrestamoId, Long estadoId, int page, int size) {
        long offset = (long) page * size;

        Mono<List<SolicitudDto>> rows = repository.findAllSolicitudesFiltered(estadoId, correo, tipoPrestamoId, size, offset).collectList();
        Mono<Long> total = repository.countAllProjected(estadoId, correo, tipoPrestamoId);

        return Mono.zip(rows, total)
                .map(t -> {
                    List<SolicitudDto> content = t.getT1();
                    long totalElements = t.getT2();
                    int totalPages = (int) ((totalElements + size - 1) / size);
                    return PageModel.<SolicitudDto>builder()
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
