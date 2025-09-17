package dev.miguel.usecase.solicitud;

import dev.miguel.model.estado.Estado;
import dev.miguel.model.estado.gateways.EstadoRepository;
import dev.miguel.model.solicitud.Solicitud;
import dev.miguel.model.solicitud.gateways.SolicitudRepository;
import dev.miguel.model.solicitud.proyections.SolicitudDto;
import dev.miguel.model.tipoprestamo.TipoPrestamo;
import dev.miguel.model.tipoprestamo.gateways.TipoPrestamoRepository;
import dev.miguel.model.utils.exceptions.BusinessException;
import dev.miguel.model.utils.exceptions.ExceptionMessages;
import dev.miguel.model.utils.exceptions.ForbiddenException;
import dev.miguel.model.utils.page.PageModel;
import dev.miguel.model.utils.sqs.QueueAlias;
import dev.miguel.model.utils.sqs.QueueCapacidadEndeudamientoMessage;
import dev.miguel.model.utils.sqs.QueueUpdateSolicitudMessage;
import dev.miguel.model.utils.sqs.gateway.IQueueService;
import dev.miguel.model.utils.userContext.UserContext;
import dev.miguel.model.utils.userContext.UserDetails;
import dev.miguel.model.utils.userContext.gateways.IGetUserDetailsById;
import dev.miguel.usecase.solicitud.validations.ValidatorSolicitudUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SolicitudUseCaseTest {

    @InjectMocks
    private SolicitudUseCase useCase;

    @Mock private SolicitudRepository solicitudRepository;
    @Mock private TipoPrestamoRepository tipoPrestamoRepository;
    @Mock private EstadoRepository estadoRepository;
    @Mock private ValidatorSolicitudUseCase validator;
    @Mock private IQueueService queueService;
    
    @Mock
    private IGetUserDetailsById getUserDetailsById;

    private static final Long ESTADO_PENDIENTE_REVISION_ID = 1L;
    private static final Long ESTADO_APROBADO_ID = 2L;
    private static final Long ESTADO_RECHAZADO_ID = 3L;
    private static final Long ESTADO_REVISION_MANUAL_ID = 4L;


    private Estado createEstado(Long id) {
        return new Estado(id, "PENDIENTE", "ESTE ES UN ESTADO");
    }

    private TipoPrestamo createTipoPrestamo(Long id, boolean validacionAutomatica) {
        return new TipoPrestamo(
                id,
                "Consumo",
                new BigDecimal(10),
                new BigDecimal(100),
                10,
                validacionAutomatica
        );
    }


    @Nested
    @DisplayName("createSolicitud")
    class createSolicitudTest {

        private Solicitud validSolicitud() {
            Solicitud s = new Solicitud();
            s.setId(1L);
            s.setMonto(new BigDecimal("1000000"));
            s.setPlazo(12);
            s.setCorreoElectronico("cliente@test.com");
            s.setTipoPrestamoId(10L);
            return s;
        }

        private Solicitud copyOf(Solicitud src) {
            Solicitud s = new Solicitud();
            s.setId(src.getId());
            s.setMonto(src.getMonto());
            s.setPlazo(src.getPlazo());
            s.setCorreoElectronico(src.getCorreoElectronico());
            s.setTipoPrestamoId(src.getTipoPrestamoId());
            s.setEstadoId(src.getEstadoId());
            s.setUsuarioId(src.getUsuarioId());
            return s;
        }


        private UserContext userContext(String id, String email, List<String> roles) {
            return new UserContext(id, email, roles);
        }

        @Test
        @DisplayName("OK: cliente crea solicitud; valida, consulta catálogos y guarda (sin validación automática)")
        void createSolicitud_ok_sin_auto() {
            // Arrange
            Solicitud input = validSolicitud();
            Solicitud persisted = copyOf(input);
            persisted.setId(99L);

            UserContext user = userContext("7", input.getCorreoElectronico(), List.of("cliente"));

            Estado estadoPendiente = createEstado(ESTADO_PENDIENTE_REVISION_ID);

            TipoPrestamo tipo = createTipoPrestamo(input.getTipoPrestamoId(),false);

            when(validator.validateCreateBody(input)).thenReturn(Mono.empty());
            when(estadoRepository.findEstadoById(ESTADO_PENDIENTE_REVISION_ID)).thenReturn(Mono.just(estadoPendiente));
            when(tipoPrestamoRepository.findTipoPrestamoById(input.getTipoPrestamoId())).thenReturn(Mono.just(tipo));
            when(solicitudRepository.saveSolicitud(any(Solicitud.class))).thenReturn(Mono.just(persisted));

            // Act / Assert
            StepVerifier.create(useCase.createSolicitud(input, user))
                    .verifyComplete(); // Esperamos que termine OK (Mono<Void> completo)

            // Captura y aserciones sobre lo que se guardó
            ArgumentCaptor<Solicitud> captor = ArgumentCaptor.forClass(Solicitud.class);
            verify(solicitudRepository).saveSolicitud(captor.capture());
            Solicitud saved = captor.getValue();

            assertEquals(ESTADO_PENDIENTE_REVISION_ID, saved.getEstadoId());
            assertEquals(Long.valueOf(user.id()), saved.getUsuarioId());
        }


        @Test
        @DisplayName("OK: con validación automática envía mensaje a la cola después de guardar")
        void createSolicitud_ok_con_auto_envia_cola() {
            // Arrange
            Solicitud input = validSolicitud();
            input.setId(null); // simulamos nueva
            Solicitud persisted = copyOf(input);
            persisted.setId(123L);

            UserContext user = userContext("7", input.getCorreoElectronico(), List.of("cliente"));

            UserDetails details = new UserDetails(
                    Long.valueOf(user.id()),
                    "Juan",
                    "Pérez",
                    "a@gmail.com",
                    new BigDecimal("2500000")
            );

            Estado estadoPendiente = createEstado(ESTADO_PENDIENTE_REVISION_ID);

            TipoPrestamo tipo = createTipoPrestamo(input.getTipoPrestamoId(), true);

            SolicitudDto aprobada = SolicitudDto.builder()
                    .usuarioId(Long.valueOf(user.id()))
                    .solicitudId(777L)
                    .monto(new BigDecimal("500000"))
                    .plazo(6)
                    .correoElectronico(input.getCorreoElectronico())
                    .tipoPrestamo("Consumo")
                    .tasaInteres(1.1d)
                    .estado("APROBADA")
                    .user(null)
                    .build();

            when(validator.validateCreateBody(input)).thenReturn(Mono.empty());
            when(estadoRepository.findEstadoById(ESTADO_PENDIENTE_REVISION_ID)).thenReturn(Mono.just(estadoPendiente));
            when(tipoPrestamoRepository.findTipoPrestamoById(input.getTipoPrestamoId())).thenReturn(Mono.just(tipo));
            when(solicitudRepository.saveSolicitud(any(Solicitud.class)))
                    .thenAnswer(invocation -> {
                        Solicitud toSave = invocation.getArgument(0);
                        // simula el "persist": misma instancia con ID asignado
                        Solicitud persistedArg = copyOf(toSave);
                        persistedArg.setId(123L);
                        return Mono.just(persistedArg);
                    });
            when(getUserDetailsById.getUserDetailsById(Long.valueOf(user.id()))).thenReturn(Mono.just(details));
            when(solicitudRepository.findAllSolicitudesAprobadasByUsuarioId(Long.valueOf(user.id()))).thenReturn(Flux.just(aprobada));
            when(queueService.send(eq(QueueAlias.CAPACIDAD_ENDEUDAMIENTO.alias()), any(QueueCapacidadEndeudamientoMessage.class)))
                    .thenReturn(Mono.empty());

            // Act / Assert
            StepVerifier.create(useCase.createSolicitud(input, user))
                    .verifyComplete();

            // Verificamos que se envió a la cola con datos coherentes
            ArgumentCaptor<QueueCapacidadEndeudamientoMessage> msgCap = ArgumentCaptor.forClass(QueueCapacidadEndeudamientoMessage.class);
            verify(queueService).send(eq(QueueAlias.CAPACIDAD_ENDEUDAMIENTO.alias()), msgCap.capture());
            QueueCapacidadEndeudamientoMessage msg = msgCap.getValue();

            assertEquals(persisted.getId(), msg.getSolicitudId());
            assertEquals(input.getMonto(), msg.getMonto());
            assertEquals(input.getPlazo(), msg.getPlazo());

            // Si tasaInteres es double, usa delta; si es BigDecimal/Double exacto, puedes dejarlo sin delta.
            assertEquals(tipo.getTasaInteres(), msg.getTasaInteres(), 0.0000001);

            assertEquals(input.getCorreoElectronico(), msg.getCorreoElectronico());
            assertEquals("Juan Pérez", msg.getNombreCompleto());
            assertEquals(details.salarioBase(), msg.getIngresosTotales());
            assertNotNull(msg.getSolicitudesActivas());
            assertFalse(msg.getSolicitudesActivas().isEmpty());
        }


        @Test
        @DisplayName("Forbidden: si el email de la solicitud no coincide con el del usuario autenticado")
        void createSolicitud_forbidden_por_email_mismatch() {
            Solicitud input = validSolicitud();
            UserContext user = userContext("7", "otro@correo.com", List.of("cliente"));

            StepVerifier.create(useCase.createSolicitud(input, user))
                    .expectErrorSatisfies(err -> {
                        assertInstanceOf(ForbiddenException.class, err);
                        assertTrue(err.getMessage().contains(ExceptionMessages.SOLICITUD_A_OTRO_USUARIO));
                    })
                    .verify();
        }

        @Test
        @DisplayName("Business: falla si el tipo de préstamo no existe (find retorna vacío)")
        void createSolicitud_tipoPrestamo_no_existe() {
            Solicitud input = validSolicitud();
            UserContext user = userContext("7", input.getCorreoElectronico(), List.of("cliente"));

            when(validator.validateCreateBody(input)).thenReturn(Mono.empty());
            when(tipoPrestamoRepository.findTipoPrestamoById(input.getTipoPrestamoId())).thenReturn(Mono.empty());
            when(estadoRepository.findEstadoById(ESTADO_PENDIENTE_REVISION_ID))
                    .thenReturn(Mono.just(createEstado(ESTADO_PENDIENTE_REVISION_ID)));

            StepVerifier.create(useCase.createSolicitud(input, user))
                    .expectErrorSatisfies(err -> {
                        assertInstanceOf(BusinessException.class, err);
                        assertTrue(err.getMessage().contains(ExceptionMessages.TIPO_PRESTAMO_NO_EXISTE));
                    })
                    .verify();
        }

        @Test
        @DisplayName("Business: falla si el estado PENDIENTE no existe (find retorna vacío)")
        void createSolicitud_estado_no_existe() {
            Solicitud input = validSolicitud();
            UserContext user = userContext("7", input.getCorreoElectronico(), List.of("cliente"));

            when(validator.validateCreateBody(input)).thenReturn(Mono.empty());
            when(tipoPrestamoRepository.findTipoPrestamoById(input.getTipoPrestamoId()))
                    .thenReturn(Mono.just(createTipoPrestamo(10L, true)));
            when(estadoRepository.findEstadoById(ESTADO_PENDIENTE_REVISION_ID)).thenReturn(Mono.empty());

            StepVerifier.create(useCase.createSolicitud(input, user))
                    .expectErrorSatisfies(err -> {
                        assertInstanceOf(BusinessException.class, err);
                        assertTrue(err.getMessage().contains(ExceptionMessages.ESTADO_DE_LA_SOLICITUD_NO_EXISTE));
                    })
                    .verify();

        }
    }

    @Nested
    @DisplayName("findAll")
    class findAllTest {


        @Test
        @DisplayName("Página vacía: devuelve la página tal cual, sin llamar a enriquecimiento")
        void findAll_pagina_vacia() {
            var user = userContext("5", "a@test.com", List.of("asesor"));
            PageModel<SolicitudDto> emptyPage = page(0, 10, 0, List.of());

            when(validator.validateFindAll(null, null, null, 0, 10)).thenReturn(Mono.empty());
            when(solicitudRepository.findAll(null, null, null, 0, 10))
                    .thenReturn(Mono.just(emptyPage));

            StepVerifier.create(useCase.findAll(null, null, null, 0, 10, user))
                    .expectNext(emptyPage)
                    .verifyComplete();
            
        }

        @Test
        @DisplayName("Página con contenido: enriquece cada DTO con user (todo OK)")
        void findAll_enriquece_ok() {
            var user = userContext("5", "a@test.com", List.of("asesor"));

            SolicitudDto dto1 = dto(101L, 11L, "u1@test.com");
            SolicitudDto dto2 = dto(102L, 12L, "u2@test.com");
            PageModel<SolicitudDto> page = page(0, 10, 2, List.of(dto1, dto2));

            UserDetails details1 = new UserDetails(11L, "Ana", "López", "miguel@gmail.com", BigDecimal.ONE);
            UserDetails details2 = new UserDetails(12L, "Luis", "Pérez", "miguel2@gmail.com", BigDecimal.ONE);

            when(validator.validateFindAll(null, null, null, 0, 10)).thenReturn(Mono.empty());
            when(solicitudRepository.findAll(null, null, null, 0, 10))
                    .thenReturn(Mono.just(page));

            when(getUserDetailsById.getUserDetailsById(dto1.getUsuarioId())).thenReturn(Mono.just(details1));
            when(getUserDetailsById.getUserDetailsById(dto2.getUsuarioId())).thenReturn(Mono.just(details2));

            StepVerifier.create(useCase.findAll(null, null, null, 0, 10, user))
                    .assertNext(p -> {
                        assertEquals(2, p.getContent().size());
                        assertSame(page, p);
                        assertEquals(details1, p.getContent().get(0).getUser());
                        assertEquals(details2, p.getContent().get(1).getUser());
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Enriquecimiento parcial: si falla para un DTO, se deja ese DTO sin user y continúa")
        void findAll_enriquecimiento_parcial_onErrorResume() {
            var user = userContext("5", "a@test.com", List.of("asesor"));

            SolicitudDto dto1 = dto(201L, 21L, "u1@test.com");
            SolicitudDto dto2 = dto(202L, 22L, "u2@test.com");
            PageModel<SolicitudDto> page = page(1, 10, 2, List.of(dto1, dto2));

            UserDetails details1 = new UserDetails(21L, "Ana", "López", "miguel@gmail.com", BigDecimal.ONE);

            when(validator.validateFindAll(null, 5L, 9L, 1, 10)).thenReturn(Mono.empty());
            when(solicitudRepository.findAll(null, 5L, 9L, 1, 10))
                    .thenReturn(Mono.just(page));

            when(getUserDetailsById.getUserDetailsById(dto1.getUsuarioId())).thenReturn(Mono.just(details1));
            when(getUserDetailsById.getUserDetailsById(dto2.getUsuarioId())).thenReturn(Mono.error(new RuntimeException("user svc down")));

            StepVerifier.create(useCase.findAll(null, 5L, 9L, 1, 10, user))
                    .assertNext(p -> {
                        assertEquals(2, p.getContent().size());
                        var first = p.getContent().get(0);
                        var second = p.getContent().get(1);
                        assertEquals(details1, first.getUser());
                        assertNull(second.getUser());
                    })
                    .verifyComplete();

            verify(getUserDetailsById).getUserDetailsById(dto1.getUsuarioId());
            verify(getUserDetailsById).getUserDetailsById(dto2.getUsuarioId());
        }

        // ===== Helpers =====

        private UserContext userContext(String id, String email, List<String> roles) {
            return new UserContext(id, email, roles);
        }

        private SolicitudDto dto(Long usuarioId, Long solicitudId, String correo) {
            return SolicitudDto.builder()
                    .usuarioId(usuarioId)
                    .solicitudId(solicitudId)
                    .correoElectronico(correo)
                    .monto(BigDecimal.ZERO)
                    .plazo(0)
                    .tipoPrestamo("PERSONAL")
                    .tasaInteres(0.0)
                    .estado("PENDIENTE")
                    .user(null)
                    .build();
        }


        private PageModel<SolicitudDto> page(int page, int size, int total, List<SolicitudDto> content) {
            PageModel<SolicitudDto> p = new PageModel<>();
            p.setPage(page);
            p.setSize(size);
            p.setTotalPages(total);
            p.setContent(new ArrayList<>(content));
            return p;
        }
    }

    @Nested
    @DisplayName("updateSolicitudTest")
    class updateSolicitudTest {

        @Test
        @DisplayName("OK: actualiza estado != REVISION_MANUAL y envía mensaje a la cola")
        void updateSolicitud_ok_envia_cola() {
            Long solicitudId = 1L;
            Long nuevoEstadoId = 200L; // distinto de REVISION_MANUAL y distinto de APROBADO
            Long usuarioId = 7L;
            Long tipoPrestamoId = 10L;

            Solicitud solicitud = new Solicitud();
            solicitud.setId(solicitudId);
            solicitud.setEstadoId(99L);
            solicitud.setUsuarioId(usuarioId);
            solicitud.setTipoPrestamoId(tipoPrestamoId);
            solicitud.setMonto(new BigDecimal("1500000"));
            solicitud.setPlazo(12);
            solicitud.setCorreoElectronico("test@mail.com");

            Estado estado = new Estado();
            estado.setId(nuevoEstadoId);
            estado.setNombre("APROBADA"); // el nombre no importa para la lógica del segundo envío; importa el ID

            TipoPrestamo tipo = createTipoPrestamo(tipoPrestamoId, true);

            SolicitudDto aprobada = SolicitudDto.builder()
                    .usuarioId(usuarioId)
                    .solicitudId(777L)
                    .monto(new BigDecimal("500000"))
                    .plazo(6)
                    .correoElectronico("test@mail.com")
                    .tipoPrestamo("Consumo")
                    .tasaInteres(1.1d)
                    .estado("APROBADA")
                    .build();

            when(solicitudRepository.findSolicitudById(solicitudId)).thenReturn(Mono.just(solicitud));
            when(estadoRepository.findEstadoById(nuevoEstadoId)).thenReturn(Mono.just(estado));
            when(tipoPrestamoRepository.findTipoPrestamoById(tipoPrestamoId)).thenReturn(Mono.just(tipo));
            when(solicitudRepository.findAllSolicitudesAprobadasByUsuarioId(usuarioId))
                    .thenReturn(Flux.just(aprobada));
            when(solicitudRepository.saveSolicitud(any(Solicitud.class)))
                    .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

            // Solo stub de la cola de actualización (esta sí debe dispararse)
            when(queueService.send(eq(QueueAlias.SOLICITUD_ACTUALIZADA.alias()), any(QueueUpdateSolicitudMessage.class)))
                    .thenReturn(Mono.just("msg-123"));

            StepVerifier.create(useCase.updateSolicitud(solicitudId, nuevoEstadoId))
                    .verifyComplete();

            // Verificar que guardó con el estado nuevo (200L)
            ArgumentCaptor<Solicitud> saveCap = ArgumentCaptor.forClass(Solicitud.class);
            verify(solicitudRepository).saveSolicitud(saveCap.capture());
            assertEquals(nuevoEstadoId, saveCap.getValue().getEstadoId());

            // Verificar que se envió a la cola de actualización con campos coherentes
            ArgumentCaptor<QueueUpdateSolicitudMessage> msgCap = ArgumentCaptor.forClass(QueueUpdateSolicitudMessage.class);
            verify(queueService).send(eq(QueueAlias.SOLICITUD_ACTUALIZADA.alias()), msgCap.capture());
            QueueUpdateSolicitudMessage msg = msgCap.getValue();

            assertEquals(solicitudId, msg.getSolicitudId());
            assertEquals("test@mail.com", msg.getCorreoElectronico());
            assertEquals("APROBADA", msg.getEstado()); // viene del nombre del Estado
            assertEquals(new BigDecimal("1500000"), msg.getMonto());
            assertEquals(12, msg.getPlazo());
            assertEquals(tipo.getTasaInteres(), msg.getTasaInteres(), 1e-9);
            assertNotNull(msg.getSolicitudesActivas());
            assertFalse(msg.getSolicitudesActivas().isEmpty());

            // Verificar que NO se envió reporte (porque el ID no es ESTADO_APROBADO_ID)
            verify(queueService, never()).send(eq(QueueAlias.REPORTE_SOLICITUD_APROBADA.alias()), any());
        }


        @Test
        @DisplayName("OK: nuevo estado REVISION_MANUAL -> NO envía mensaje a la cola")
        void updateSolicitud_ok_revision_manual_no_envia_cola() {
            Long solicitudId = 1L;
            Long nuevoEstadoId = ESTADO_REVISION_MANUAL_ID; // constante del dominio
            Long usuarioId = 7L;
            Long tipoPrestamoId = 10L;

            Solicitud solicitud = new Solicitud();
            solicitud.setId(solicitudId);
            solicitud.setEstadoId(99L);
            solicitud.setUsuarioId(usuarioId);
            solicitud.setTipoPrestamoId(tipoPrestamoId);
            solicitud.setMonto(new BigDecimal("1200000"));
            solicitud.setPlazo(24);
            solicitud.setCorreoElectronico("test@mail.com");

            Estado estado = new Estado();
            estado.setId(nuevoEstadoId);
            estado.setNombre("REVISION_MANUAL");

            when(solicitudRepository.findSolicitudById(solicitudId)).thenReturn(Mono.just(solicitud));
            when(estadoRepository.findEstadoById(nuevoEstadoId)).thenReturn(Mono.just(estado));

            when(solicitudRepository.saveSolicitud(any(Solicitud.class)))
                    .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

            StepVerifier.create(useCase.updateSolicitud(solicitudId, nuevoEstadoId))
                    .verifyComplete();

            // Guardó con REVISION_MANUAL
            ArgumentCaptor<Solicitud> saveCap = ArgumentCaptor.forClass(Solicitud.class);
            verify(solicitudRepository).saveSolicitud(saveCap.capture());
            assertEquals(ESTADO_REVISION_MANUAL_ID, saveCap.getValue().getEstadoId());

        }

        @Test
        @DisplayName("Solicitud no existe -> BusinessException")
        void updateSolicitud_notFoundSolicitud() {
            Long solicitudId = 1L;
            Long estadoId = 2L;

            when(solicitudRepository.findSolicitudById(solicitudId)).thenReturn(Mono.empty());

            StepVerifier.create(useCase.updateSolicitud(solicitudId, estadoId))
                    .expectErrorSatisfies(err -> {
                        assertInstanceOf(BusinessException.class, err);
                        assertEquals(ExceptionMessages.SOLICITUD_NO_EXISTE, err.getMessage());
                    })
                    .verify();
        }

        @Test
        @DisplayName("Estado no existe -> BusinessException")
        void updateSolicitud_estadoNoExiste() {
            Long solicitudId = 1L;
            Long estadoId = 2L;

            Solicitud solicitud = new Solicitud();
            solicitud.setId(solicitudId);
            solicitud.setEstadoId(99L);

            when(solicitudRepository.findSolicitudById(solicitudId)).thenReturn(Mono.just(solicitud));
            when(estadoRepository.findEstadoById(estadoId)).thenReturn(Mono.empty());

            StepVerifier.create(useCase.updateSolicitud(solicitudId, estadoId))
                    .expectErrorSatisfies(err -> {
                        assertInstanceOf(BusinessException.class, err);
                        assertEquals(ExceptionMessages.ESTADO_DE_LA_SOLICITUD_NO_EXISTE, err.getMessage());
                    })
                    .verify();

        }

        @Test
        @DisplayName("Solicitud ya revisada (APROBADA o RECHAZADA) -> BusinessException")
        void updateSolicitud_yaRevisada() {
            Long solicitudId = 1L;
            Long nuevoEstadoId = 200L;

            // Probar ambos casos: aprobada y rechazada
            for (Long estadoActual : List.of(ESTADO_APROBADO_ID, ESTADO_RECHAZADO_ID)) {
                Solicitud s = new Solicitud();
                s.setId(solicitudId);
                s.setEstadoId(estadoActual);

                when(solicitudRepository.findSolicitudById(solicitudId)).thenReturn(Mono.just(s));

                StepVerifier.create(useCase.updateSolicitud(solicitudId, nuevoEstadoId))
                        .expectErrorSatisfies(err -> {
                            assertInstanceOf(BusinessException.class, err);
                            assertEquals(ExceptionMessages.SOLICITUD_YA_REVISADA, err.getMessage());
                        })
                        .verify();

                // limpieza del stub para la siguiente iteración
                clearInvocations(solicitudRepository);
            }
        }
    }

}
