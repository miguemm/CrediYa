package dev.miguel.usecase.solicitud;

import dev.miguel.model.estado.gateways.EstadoRepository;
import dev.miguel.model.solicitud.proyections.SolicitudDto;
import dev.miguel.model.utils.exceptions.BusinessException;
import dev.miguel.model.utils.exceptions.ForbiddenException;
import dev.miguel.model.solicitud.Solicitud;
import dev.miguel.model.solicitud.gateways.SolicitudRepository;
import dev.miguel.model.tipoprestamo.gateways.TipoPrestamoRepository;
import dev.miguel.model.utils.page.PageModel;
import dev.miguel.model.utils.sqs.SQSMessage;
import dev.miguel.model.utils.sqs.gateway.ISQSService;
import dev.miguel.model.utils.userContext.UserContext;
import dev.miguel.model.utils.userContext.gateways.IGetUserDetailsById;
import dev.miguel.usecase.solicitud.gateways.ISolicitudUseCase;
import dev.miguel.model.utils.exceptions.ExceptionMessages;
import dev.miguel.usecase.solicitud.validations.ValidatorSolicitudUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SolicitudUseCase implements ISolicitudUseCase {

    private static final Long ESTADO_PENDIENTE_REVISION_ID = 1L;

    private final SolicitudRepository solicitudRepository;
    private final EstadoRepository estadoRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;

    private final IGetUserDetailsById getUserDetailsById;
    private final ISQSService sqsService;

    private final ValidatorSolicitudUseCase validator;

    @Override
    public Mono<Void> createSolicitud(Solicitud solicitud, UserContext user) {

        if (!emailsMatch(solicitud.getCorreoElectronico(), user.email())) {
            return Mono.error(new ForbiddenException(ExceptionMessages.SOLICITUD_A_OTRO_USUARIO));
        }

        return validator.validateCreateBody(solicitud)
            .then(Mono.zip(
                tipoPrestamoRepository.existsTipoPrestamoById(solicitud.getTipoPrestamoId()),
                estadoRepository.existsEstadoById(ESTADO_PENDIENTE_REVISION_ID)
            ))
            .flatMap(exists -> {
                boolean tipoOk = exists.getT1();
                boolean estadoOk = exists.getT2();

                if (!tipoOk)   return Mono.error(new BusinessException(ExceptionMessages.TIPO_PRESTAMO_NO_EXISTE));
                if (!estadoOk) return Mono.error(new BusinessException(ExceptionMessages.ESTADO_DE_LA_SOLICITUD_NO_EXISTE));

                solicitud.setEstadoId(ESTADO_PENDIENTE_REVISION_ID);
                solicitud.setUsuarioId(Long.valueOf(user.id()));
                return solicitudRepository.saveSolicitud(solicitud).then();
            });
    }

    @Override
    public Mono<Void> updateSolicitud(Long solicitudId, Long estadoId) {
        return solicitudRepository.findSolicitudById(solicitudId)
            .switchIfEmpty(Mono.error(new BusinessException(ExceptionMessages.SOLICITUD_NO_EXISTE)))
            .flatMap(solicitud ->
                estadoRepository.findEstadoById(estadoId)
                    .switchIfEmpty(Mono.error(new BusinessException(ExceptionMessages.ESTADO_DE_LA_SOLICITUD_NO_EXISTE)))
                    .flatMap(estado -> {

                        solicitud.setEstadoId(estado.getId());
                        return solicitudRepository.saveSolicitud(solicitud)
                            .flatMap(saved ->
                                sqsService.send(
                                    SQSMessage.builder()
                                        .solicitudId(saved.getId())
                                        .correoElectronico(saved.getCorreoElectronico())
                                        .estado(estado.getNombre())
                                        .build()
                                )
                            );
                    })
            )
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
                        getUserDetailsById.getUserDetailsById(dto)
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
