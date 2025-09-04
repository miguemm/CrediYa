package dev.miguel.usecase.solicitud;

import dev.miguel.model.estado.gateways.EstadoRepository;
import dev.miguel.model.solicitud.proyections.SolicitudDto;
import dev.miguel.model.utils.exception.BusinessException;
import dev.miguel.model.utils.exception.ForbiddenException;
import dev.miguel.model.solicitud.Solicitud;
import dev.miguel.model.solicitud.gateways.SolicitudRepository;
import dev.miguel.model.tipoprestamo.gateways.TipoPrestamoRepository;
import dev.miguel.model.utils.page.PageModel;
import dev.miguel.model.utils.userContext.UserContext;
import dev.miguel.model.utils.userContext.gateways.IGetUserDetailsById;
import dev.miguel.usecase.solicitud.gateways.ISolicitudUseCase;
import dev.miguel.model.utils.exception.ExceptionMessages;
import dev.miguel.usecase.solicitud.validations.FindAllValidator;
import dev.miguel.usecase.solicitud.validations.SolicitudValidator;
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


    @Override
    public Mono<Void> createSolicitud(Solicitud solicitud, UserContext user) {
        SolicitudValidator validator = new SolicitudValidator();

        return Mono.defer(() -> {
            // Debe tener rol "cliente"
            if (!hasSpecificRole(user, "cliente")) {
                return Mono.error(new ForbiddenException("Solo los clientes pueden crear solicitudes."));
            }

            // El email de la solicitud debe coincidir con el del usuario autenticado
            if (!emailsMatch(solicitud.getCorreoElectronico(), user.email())) {
                return Mono.error(new ForbiddenException("No puedes crear solicitudes en nombre de otro usuario."));
            }

            return validator.validateAll(solicitud)
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
        });
    }

    @Override
    public Mono<PageModel<SolicitudDto>> findAll(String correoElectronico, Long tipoPrestamoId, Long estadoId, Integer page, Integer size, UserContext user) {
        FindAllValidator validator = new FindAllValidator();

        return Mono.defer(() -> {
            if (!hasSpecificRole(user, "asesor")) {
                return Mono.error(new ForbiddenException("Solo los asesores pueden listar solicitudes."));
            }

            return validator.validate(correoElectronico, tipoPrestamoId, estadoId, page, size)
                    .then(solicitudRepository.findAll(correoElectronico, tipoPrestamoId, estadoId, page, size))
                    .flatMap(pageable -> {
                        if (pageable.getContent().isEmpty()) {
                            return Mono.just(pageable);
                        }

                        return Flux.fromIterable(pageable.getContent())
                                .flatMap(dto ->
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
        });
    }

    private boolean hasSpecificRole(UserContext user, String authorizedRol) {
        return user != null
                && user.roles() != null
                && user.roles().stream().anyMatch(r -> authorizedRol.equalsIgnoreCase(r.trim()));
    }

    private boolean emailsMatch(String a, String b) {
        return a != null && b != null && a.trim().equalsIgnoreCase(b.trim());
    }
}
