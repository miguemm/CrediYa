package dev.miguel.usecase.solicitud;

import dev.miguel.model.estado.Estado;
import dev.miguel.model.estado.gateways.EstadoRepository;
import dev.miguel.model.solicitud.proyections.SolicitudDto;
import dev.miguel.model.tipoprestamo.TipoPrestamo;
import dev.miguel.model.utils.exceptions.BusinessException;
import dev.miguel.model.utils.exceptions.ForbiddenException;
import dev.miguel.model.solicitud.Solicitud;
import dev.miguel.model.solicitud.gateways.SolicitudRepository;
import dev.miguel.model.tipoprestamo.gateways.TipoPrestamoRepository;
import dev.miguel.model.utils.page.PageModel;
import dev.miguel.model.utils.sqs.QueueAlias;
import dev.miguel.model.utils.sqs.QueueCapacidadEndeudamientoMessage;
import dev.miguel.model.utils.sqs.QueueUpdateSolicitudMessage;
import dev.miguel.model.utils.sqs.gateway.IQueueService;
import dev.miguel.model.utils.userContext.UserContext;
import dev.miguel.model.utils.userContext.UserDetails;
import dev.miguel.model.utils.userContext.gateways.IGetUserDetailsById;
import dev.miguel.usecase.solicitud.gateways.ISolicitudUseCase;
import dev.miguel.model.utils.exceptions.ExceptionMessages;
import dev.miguel.usecase.solicitud.validations.ValidatorSolicitudUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class SolicitudUseCase implements ISolicitudUseCase {

    private static final Long ESTADO_PENDIENTE_REVISION_ID = 1L;
    private static final Long ESTADO_APROBADO_ID = 2L;
    private static final Long ESTADO_RECHAZADO_ID = 3L;
    private static final Long ESTADO_REVISION_MANUAL_ID = 4L;

    private final SolicitudRepository solicitudRepository;
    private final EstadoRepository estadoRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;

    private final IGetUserDetailsById getUserDetailsById;
    private final IQueueService queueService;

    private final ValidatorSolicitudUseCase validator;

    @Override
    public Mono<Void> createSolicitud(Solicitud solicitud, UserContext user) {

        if (!emailsMatch(solicitud.getCorreoElectronico(), user.email())) {
            return Mono.error(new ForbiddenException(ExceptionMessages.SOLICITUD_A_OTRO_USUARIO));
        }

        return validator.validateCreateBody(solicitud)
            .then(
                Mono.zip(
                    tipoPrestamoRepository.findTipoPrestamoById(solicitud.getTipoPrestamoId())
                            .switchIfEmpty(Mono.error(new BusinessException(ExceptionMessages.TIPO_PRESTAMO_NO_EXISTE))),
                    estadoRepository.findEstadoById(ESTADO_PENDIENTE_REVISION_ID)
                            .switchIfEmpty(Mono.error(new BusinessException(ExceptionMessages.ESTADO_DE_LA_SOLICITUD_NO_EXISTE)))
                )
            )
            .flatMap(exists -> {

                TipoPrestamo tipoPrestamo = exists.getT1();
                Estado estado = exists.getT2();

                solicitud.setEstadoId(estado.getId());
                solicitud.setUsuarioId(Long.valueOf(user.id()));

                return solicitudRepository.saveSolicitud(solicitud)
                    .flatMap(nuevaSolicitud -> {

                        if (!tipoPrestamo.isValidacionAutomatica()) {
                            return Mono.empty();
                        }

                        return getUserDetailsById.getUserDetailsById(nuevaSolicitud.getUsuarioId())
                            .switchIfEmpty(Mono.error(new BusinessException(ExceptionMessages.USUARIO_NO_EXISTE)))
                            .zipWith(
                                    solicitudRepository.findAllSolicitudesAprobadasByUsuarioId(Long.valueOf(user.id()))
                                            .collectList()
                            )
                            .flatMap(tuple -> {
                                UserDetails usuario = tuple.getT1();
                                List<SolicitudDto> solicitudes = tuple.getT2();

                                 return queueService.send(
                                    QueueAlias.CAPACIDAD_ENDEUDAMIENTO.alias(),
                                    QueueCapacidadEndeudamientoMessage.builder()
                                            .solicitudId(nuevaSolicitud.getId())
                                            .monto(solicitud.getMonto())
                                            .plazo(solicitud.getPlazo())
                                            .tasaInteres(tipoPrestamo.getTasaInteres())
                                            .correoElectronico(solicitud.getCorreoElectronico())
                                            .nombreCompleto(usuario.nombres() + ' ' + usuario.apellidos())
                                            .ingresosTotales(usuario.salarioBase())
                                            .solicitudesActivas(solicitudes)
                                            .build()
                                 );
                            }).then();
                    });
            });
    }

    @Override
    public Mono<Void> updateSolicitud(Long solicitudId, Long estadoId) {
        return solicitudRepository.findSolicitudById(solicitudId)
                .switchIfEmpty(Mono.error(new BusinessException(ExceptionMessages.SOLICITUD_NO_EXISTE)))
                .flatMap(solicitud -> {
                        if (Objects.equals(solicitud.getEstadoId(), ESTADO_APROBADO_ID) | Objects.equals(solicitud.getEstadoId(), ESTADO_RECHAZADO_ID)) {
                            return Mono.error(new BusinessException(ExceptionMessages.SOLICITUD_YA_REVISADA));
                        }
                        return estadoRepository.findEstadoById(estadoId)
                                .switchIfEmpty(Mono.error(new BusinessException(ExceptionMessages.ESTADO_DE_LA_SOLICITUD_NO_EXISTE)))
                                .flatMap(estado -> {
                                    // Actualizamos el estado y guardamos
                                    solicitud.setEstadoId(estado.getId());

                                    return solicitudRepository.saveSolicitud(solicitud)
                                            .flatMap(nuevaSolicitud -> {

                                                // Si el nuevo estado es revision manual no enviamos notificación a SQS
                                                if (Objects.equals(solicitud.getEstadoId(), ESTADO_REVISION_MANUAL_ID)) {
                                                    return Mono.empty();
                                                }

                                                return Mono.zip(
                                                                // 1) La solicitud ya guardada
                                                                Mono.just(nuevaSolicitud),

                                                                // 2) Las solicitudes aprobadas del usuario (después del save)
                                                                solicitudRepository.findAllSolicitudesAprobadasByUsuarioId(nuevaSolicitud.getUsuarioId())
                                                                        .collectList(),

                                                                // 3) El tipo de préstamo de la solicitud
                                                                tipoPrestamoRepository.findTipoPrestamoById(nuevaSolicitud.getTipoPrestamoId())
                                                                        .switchIfEmpty(Mono.error(new BusinessException(ExceptionMessages.TIPO_PRESTAMO_NO_EXISTE)))
                                                        )
                                                        .flatMap(tuple3 -> {
                                                            var saved = tuple3.getT1();
                                                            var solicitudesActivas = tuple3.getT2();
                                                            var tipoPrestamo = tuple3.getT3();

                                                            // Construimos el mensaje para la cola
                                                            var message = QueueUpdateSolicitudMessage.builder()
                                                                    .solicitudId(saved.getId())
                                                                    .correoElectronico(saved.getCorreoElectronico())
                                                                    .estado(estado.getNombre())
                                                                    .monto(solicitud.getMonto())
                                                                    .plazo(solicitud.getPlazo())
                                                                    .tasaInteres(tipoPrestamo.getTasaInteres())
                                                                    .solicitudesActivas(solicitudesActivas)
                                                                    .build();

                                                            return queueService.send(QueueAlias.SOLICITUD_ACTUALIZADA.alias(), message);
                                                        });
                                            });
                                });
                })
                .then();
    }


    @Override
    public Mono<PageModel<SolicitudDto>> findAll(String correoElectronico, Long tipoPrestamoId, Long estadoId, Integer page, Integer size, UserContext user) {
        return validator.validateFindAll(correoElectronico, tipoPrestamoId, estadoId, page, size)
            .then(solicitudRepository.findAll(correoElectronico, tipoPrestamoId, estadoId, page, size))
            .flatMap(pageable -> {
                if (pageable.getContent().isEmpty()) {
                    return Mono.just(pageable);
                }

                return Flux.fromIterable(pageable.getContent())
                    .flatMapSequential(dto ->
                        getUserDetailsById.getUserDetailsById(dto.getUsuarioId())
                            .map(userDetails -> {
                                dto.setUser(userDetails);
                                return dto;
                            })
                            .onErrorResume(e -> Mono.just(dto)), 10

                    )
                    .collectList()
                    .map(list -> {
                        pageable.setContent(list);
                        return pageable;
                    });
            });
    }

    private boolean emailsMatch(String a, String b) {
        return a != null && b != null && a.trim().equalsIgnoreCase(b.trim());
    }

}
