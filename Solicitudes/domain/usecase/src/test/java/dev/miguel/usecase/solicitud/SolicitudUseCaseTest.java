package dev.miguel.usecase.solicitud;

import dev.miguel.model.estado.gateways.EstadoRepository;
import dev.miguel.model.solicitud.Solicitud;
import dev.miguel.model.solicitud.gateways.SolicitudRepository;
import dev.miguel.model.solicitud.proyections.SolicitudDto;
import dev.miguel.model.tipoprestamo.gateways.TipoPrestamoRepository;
import dev.miguel.model.utils.exceptions.BusinessException;
import dev.miguel.model.utils.exceptions.ExceptionMessages;
import dev.miguel.model.utils.exceptions.ForbiddenException;
import dev.miguel.model.utils.page.PageModel;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SolicitudUseCaseTest {

    @InjectMocks
    private SolicitudUseCase useCase;

    @Mock private SolicitudRepository solicitudRepository;
    @Mock private TipoPrestamoRepository tipoPrestamoRepository;
    @Mock private EstadoRepository estadoRepository;
    @Mock private ValidatorSolicitudUseCase validator;
    
    @Mock
    private IGetUserDetailsById getUserDetailsById;

    private static final Long ESTADO_PENDIENTE_REVISION_ID = 1L;


    @Nested
    @DisplayName("createSolicitud")
    class createSolicitudTest {

        @Test
        @DisplayName("OK: cliente crea solicitud con email propio; valida, verifica catálogos y guarda")
        void createSolicitud_ok() {
            Solicitud input = validSolicitud();
            UserContext user = userContext("7", input.getCorreoElectronico(), List.of("cliente"));

            when(validator.validateCreateBody(input)).thenReturn(Mono.empty());
            when(tipoPrestamoRepository.existsTipoPrestamoById(input.getTipoPrestamoId())).thenReturn(Mono.just(true));
            when(estadoRepository.existsEstadoById(ESTADO_PENDIENTE_REVISION_ID)).thenReturn(Mono.just(true));
            when(solicitudRepository.saveSolicitud(any(Solicitud.class))).thenReturn(Mono.empty());

            StepVerifier.create(useCase.createSolicitud(input, user))
                    .verifyComplete();

            ArgumentCaptor<Solicitud> captor = ArgumentCaptor.forClass(Solicitud.class);
            verify(solicitudRepository).saveSolicitud(captor.capture());
            Solicitud saved = captor.getValue();

            assertEquals(ESTADO_PENDIENTE_REVISION_ID, saved.getEstadoId());
            assertEquals(Long.valueOf(user.id()), saved.getUsuarioId());

        }

        @Test
        @DisplayName("Forbidden: si el usuario no tiene rol 'cliente'")
        void createSolicitud_forbidden_por_rol() {
            Solicitud input = validSolicitud();
            UserContext user = userContext("7", input.getCorreoElectronico(), List.of("admin"));

            StepVerifier.create(useCase.createSolicitud(input, user))
                    .expectErrorSatisfies(err -> {
                        assertTrue(err instanceof ForbiddenException);
                        assertTrue(err.getMessage().contains("Solo los clientes pueden crear solicitudes."));
                    })
                    .verify();
        }

        @Test
        @DisplayName("Forbidden: si el email de la solicitud no coincide con el del usuario autenticado")
        void createSolicitud_forbidden_por_email_mismatch() {
            Solicitud input = validSolicitud();
            UserContext user = userContext("7", "otro@correo.com", List.of("cliente"));

            StepVerifier.create(useCase.createSolicitud(input, user))
                    .expectErrorSatisfies(err -> {
                        assertTrue(err instanceof ForbiddenException);
                        assertTrue(err.getMessage().contains("No puedes crear solicitudes en nombre de otro usuario."));
                    })
                    .verify();
            
        }

        @Test
        @DisplayName("Business: falla si el tipo de préstamo no existe")
        void createSolicitud_tipoPrestamo_no_existe() {
            Solicitud input = validSolicitud();
            UserContext user = userContext("7", input.getCorreoElectronico(), List.of("cliente"));

            when(validator.validateCreateBody(input)).thenReturn(Mono.empty());
            when(tipoPrestamoRepository.existsTipoPrestamoById(input.getTipoPrestamoId())).thenReturn(Mono.just(false));
            // Igualmente se consulta el estado porque se usa Mono.zip
            when(estadoRepository.existsEstadoById(ESTADO_PENDIENTE_REVISION_ID)).thenReturn(Mono.just(true));

            StepVerifier.create(useCase.createSolicitud(input, user))
                    .expectErrorSatisfies(err -> {
                        assertTrue(err instanceof BusinessException);
                        assertTrue(err.getMessage().contains(ExceptionMessages.TIPO_PRESTAMO_NO_EXISTE));
                    })
                    .verify();
            
        }

        @Test
        @DisplayName("Business: falla si el estado PENDIENTE no existe")
        void createSolicitud_estado_no_existe() {
            Solicitud input = validSolicitud();
            UserContext user = userContext("7", input.getCorreoElectronico(), List.of("cliente"));

            when(validator.validateCreateBody(input)).thenReturn(Mono.empty());
            when(tipoPrestamoRepository.existsTipoPrestamoById(input.getTipoPrestamoId())).thenReturn(Mono.just(true));
            when(estadoRepository.existsEstadoById(ESTADO_PENDIENTE_REVISION_ID)).thenReturn(Mono.just(false));

            StepVerifier.create(useCase.createSolicitud(input, user))
                    .expectErrorSatisfies(err -> {
                        assertTrue(err instanceof BusinessException);
                        assertTrue(err.getMessage().contains(ExceptionMessages.ESTADO_DE_LA_SOLICITUD_NO_EXISTE));
                    })
                    .verify();
            
        }

        // ===== Helpers =====

        private Solicitud validSolicitud() {
            Solicitud s = new Solicitud();
            s.setId(1L);
            s.setMonto(new BigDecimal("1000000"));
            s.setPlazo(12);
            s.setCorreoElectronico("cliente@test.com");
            s.setTipoPrestamoId(10L);
            return s;
        }

        private UserContext userContext(String id, String email, List<String> roles) {
            return new UserContext(id, email, roles);
        }
    }

    @Nested
    @DisplayName("findAll")
    class findAllTest {
        
        @Test
        @DisplayName("Forbidden: solo asesores pueden listar solicitudes")
        void findAll_forbidden_por_rol() {
            var user = userContext("5", "asesorX@test.com", List.of("cliente")); // no es asesor

            StepVerifier.create(useCase.findAll(null, null, null, 0, 10, user))
                    .expectErrorSatisfies(err -> {
                        assertTrue(err instanceof ForbiddenException);
                        assertTrue(err.getMessage().contains("Solo los asesores pueden listar solicitudes."));
                    })
                    .verify();
            
        }

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

            when(getUserDetailsById.getUserDetailsById(dto1)).thenReturn(Mono.just(details1));
            when(getUserDetailsById.getUserDetailsById(dto2)).thenReturn(Mono.just(details2));

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

            when(getUserDetailsById.getUserDetailsById(dto1)).thenReturn(Mono.just(details1));
            when(getUserDetailsById.getUserDetailsById(dto2)).thenReturn(Mono.error(new RuntimeException("user svc down")));

            StepVerifier.create(useCase.findAll(null, 5L, 9L, 1, 10, user))
                    .assertNext(p -> {
                        assertEquals(2, p.getContent().size());
                        var first = p.getContent().get(0);
                        var second = p.getContent().get(1);
                        assertEquals(details1, first.getUser());
                        assertNull(second.getUser());
                    })
                    .verifyComplete();

            verify(getUserDetailsById).getUserDetailsById(dto1);
            verify(getUserDetailsById).getUserDetailsById(dto2);
        }

        // ===== Helpers =====

        private UserContext userContext(String id, String email, List<String> roles) {
            return new UserContext(id, email, roles);
        }

        private SolicitudDto dto(Long solicitudId, Long usuarioId, String correo) {
            SolicitudDto d = new SolicitudDto();
            d.setSolicitudId(solicitudId);
            d.setUsuarioId(usuarioId);
            d.setCorreoElectronico(correo);
            // d.setUser(null) por defecto
            return d;
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

}
