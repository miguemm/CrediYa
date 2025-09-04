package dev.miguel.usecase.solicitud.validations;

import dev.miguel.model.solicitud.Solicitud;
import dev.miguel.usecase.exception.ArgumentException;
import dev.miguel.model.utils.exceptions.ExceptionMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class SolicitudValidatorTest {

    @InjectMocks
    private SolicitudValidator solicitudValidator;

    private Solicitud solicitud() {
        return Solicitud.builder()
            .id(1L)
            .monto(BigDecimal.valueOf(10000000))
            .plazo(1)
            .correoElectronico("mandresmosquera@gmail.com")
            .estadoId(1L)
            .tipoPrestamoId(1L)
            .build();
    }

    @Test
    @DisplayName("Validate all ok")
    void validateAll_ok() {
        StepVerifier.create(solicitudValidator.validateAll(solicitud()))
                .verifyComplete();
    }

    @Nested
    @DisplayName("Validator ")
    class NombreTests {

        private void expectArgumentExceptionWithMessages(Throwable t, List<String> expected) {
            assertInstanceOf(ArgumentException.class, t, "Debe lanzar ArgumentException");

            ArgumentException ex = (ArgumentException) t;
            assertNotNull(ex.getErrors(), "La lista de errores no debe ser null");
            assertEquals(expected, ex.getErrors(), "Errores agregados y en orden esperado");
        }

        @Test
        void montoNull() {
            Solicitud solicitud = solicitud().toBuilder().monto(null).build();

            StepVerifier.create(solicitudValidator.validateAll(solicitud))
                    .expectErrorSatisfies(t -> {
                        expectArgumentExceptionWithMessages(t, List.of(ExceptionMessages.CAMPO_MONTO_INVALIDO));
                    })
                    .verify();
        }

        @Test
        void montoNegative() {
            // Arrange
            Solicitud solicitud = solicitud().toBuilder().monto(BigDecimal.valueOf(-5)).build();

            // Act + Assert
            StepVerifier.create(solicitudValidator.validateAll(solicitud))
                    .expectErrorSatisfies(t -> {
                        expectArgumentExceptionWithMessages(t, List.of(ExceptionMessages.CAMPO_MONTO_INVALIDO));
                    })
                    .verify();
        }

        @Test
        void plazoNull() {
            Solicitud solicitud = solicitud().toBuilder().plazo(null).build();

            StepVerifier.create(solicitudValidator.validateAll(solicitud))
                    .expectErrorSatisfies(t -> {
                        expectArgumentExceptionWithMessages(t, List.of(ExceptionMessages.CAMPO_PLAZO_INVALIDO));
                    })
                    .verify();
        }

        @Test
        void plazoNegative() {
            // Arrange
            Solicitud solicitud = solicitud().toBuilder().plazo(-1).build();

            // Act + Assert
            StepVerifier.create(solicitudValidator.validateAll(solicitud))
                    .expectErrorSatisfies(t -> {
                        expectArgumentExceptionWithMessages(t, List.of(ExceptionMessages.CAMPO_PLAZO_INVALIDO));
                    })
                    .verify();
        }

        @Test
        void correoNull() {
            Solicitud solicitud = solicitud().toBuilder().correoElectronico(null).build();

            StepVerifier.create(solicitudValidator.validateAll(solicitud))
                    .expectErrorSatisfies(t -> {
                        expectArgumentExceptionWithMessages(t, List.of(ExceptionMessages.CAMPO_EMAIL_INVALIDO));
                    })
                    .verify();
        }

        @Test
        void emailVacio() {
            // Arrange
            Solicitud solicitud = solicitud().toBuilder().correoElectronico("").build();

            // Act + Assert
            StepVerifier.create(solicitudValidator.validateAll(solicitud))
                    .expectErrorSatisfies(t -> {
                        expectArgumentExceptionWithMessages(t, List.of(ExceptionMessages.CAMPO_EMAIL_INVALIDO));
                    })
                    .verify();
        }

        @Test
        void emailNoValido() {
            // Arrange
            Solicitud solicitud = solicitud().toBuilder().correoElectronico("abc").build();

            // Act + Assert
            StepVerifier.create(solicitudValidator.validateAll(solicitud))
                    .expectErrorSatisfies(t -> {
                        expectArgumentExceptionWithMessages(t, List.of(ExceptionMessages.FORMATO_EMAIL_INVALIDO));
                    })
                    .verify();
        }


    }

}
