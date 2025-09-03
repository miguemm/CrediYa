package dev.miguel.r2dbc;

import dev.miguel.model.solicitud.proyections.findAllSolicitudes;
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
          s.solicitud_id AS solicitudId,
          s.monto AS monto,
          s.plazo AS plazo,
          s.correo_electronico AS correoElectronico,
          s.estado_id AS estadoId,
          s.tipo_prestamo_id AS tipoPrestamoId,
          s.usuario_id AS usuarioId
        FROM solicitud s
        WHERE (:estadoId IS NULL OR s.estado_id = :estadoId)
          AND (:correo IS NULL OR s.correo_electronico = :correo)
          AND (:tipoPrestamoId IS NULL OR s.tipo_prestamo_id = :tipoPrestamoId)
        ORDER BY s.solicitud_id DESC
        LIMIT :limit OFFSET :offset
    """)
    Flux<findAllSolicitudes> findAllProjected(
            @Param("estadoId") Long estadoId,
            @Param("correo") String correo,
            @Param("tipoPrestamoId") Long tipoPrestamoId,
            @Param("limit") long limit,
            @Param("offset") long offset
    );

    @Query("""
        SELECT COUNT(*) 
        FROM solicitud s
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
