package dev.miguel.r2dbc;

import dev.miguel.model.solicitud.proyections.SolicitudDto;
import dev.miguel.r2dbc.entity.SolicitudEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SolicitudReactiveRepository extends ReactiveCrudRepository<SolicitudEntity, Long>, ReactiveQueryByExampleExecutor<SolicitudEntity> {

    @Query("""
        SELECT
            s.usuario_id AS usuario_id,
             s.solicitud_id AS solicitud_id,
             s.monto,
             s.plazo,
             s.correo_electronico AS correo_electronico,
             tipo.nombre     AS tipo_prestamo,
             estado.nombre   AS estado
           FROM solicitud s
           JOIN tipo_prestamo tipo ON tipo.tipo_prestamo_id = s.tipo_prestamo_id
           JOIN estado ON estado.estado_id = s.estado_id
           WHERE (:estadoId IS NULL OR s.estado_id = :estadoId)
             AND (:correo IS NULL OR s.correo_electronico = :correo)
             AND (:tipoPrestamoId IS NULL OR s.tipo_prestamo_id = :tipoPrestamoId)
           ORDER BY s.solicitud_id DESC
           LIMIT :limit OFFSET :offset
    """)
    Flux<SolicitudDto> findAllProjected(
            @Param("estadoId") Long estadoId,
            @Param("correo") String correo,
            @Param("tipoPrestamoId") Long tipoPrestamoId,
            @Param("limit") long limit,
            @Param("offset") long offset
    );

    @Query("""
        SELECT COUNT(*) 
        FROM solicitud s
        JOIN tipo_prestamo tipo on tipo.tipo_prestamo_id = s.tipo_prestamo_id
        JOIN estado on estado.estado_id = s.estado_id
        WHERE (:estadoId IS NULL OR s.estado_id = :estadoId)
          AND (:correo IS NULL OR s.correo_electronico = :correo)
          AND (:tipoPrestamoId IS NULL OR s.tipo_prestamo_id = :tipoPrestamoId)
    """)
    Mono<Long> countAllProjected(
            @Param("estadoId") Long estadoId,
            @Param("correo") String correo,
            @Param("tipoPrestamoId") Long tipoPrestamoId
    );


}
