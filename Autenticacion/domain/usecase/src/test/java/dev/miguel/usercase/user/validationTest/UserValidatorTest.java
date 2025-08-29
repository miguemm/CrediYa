package dev.miguel.usercase.user.validationTest;

import dev.miguel.model.user.User;
import dev.miguel.usecase.exception.ArgumentException;
import dev.miguel.usecase.exception.ExceptionMessages;
import dev.miguel.usecase.user.validation.UserValidator;
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
class UserValidatorTest {

    @InjectMocks
    private UserValidator executor;

    // --- Helpers ---
    private User validUser() {
        return User.builder()
                .nombres("Miguel")
                .apellidos("Mosquera")
                .salarioBase(new BigDecimal("2500000"))
                .correoElectronico("miguel@test.com")
                .build();
    }

    private void expectArgumentExceptionWithMessages(Throwable t, List<String> expected) {
        assertInstanceOf(ArgumentException.class, t, "Debe lanzar ArgumentException");

        ArgumentException ex = (ArgumentException) t;
        assertNotNull(ex.getErrors(), "La lista de errores no debe ser null");
        assertEquals(expected, ex.getErrors(), "Errores agregados y en orden esperado");
    }

    @Test
    @DisplayName("Caso feliz: usuario válido -> Mono.empty()")
    void validUserShouldCompleteEmpty() {
        User user = validUser();

        StepVerifier.create(executor.validateAll(user))
                .verifyComplete();
    }

    @Nested
    @DisplayName("Validación de nombre")
    class NombreTests {

        @Test
        void nombreNull() {
            User user = validUser().toBuilder().nombres(null).build();

            StepVerifier.create(executor.validateAll(user))
                    .expectErrorSatisfies(t ->
                            expectArgumentExceptionWithMessages(
                                    t,
                                    List.of(ExceptionMessages.CAMPO_NOMBRE_INVALIDO)
                            )
                    )
                    .verify();
        }

        @Test
        void nombreVacio() {
            User user = validUser().toBuilder().nombres("").build();

            StepVerifier.create(executor.validateAll(user))
                    .expectErrorSatisfies(t ->
                            expectArgumentExceptionWithMessages(
                                    t,
                                    List.of(ExceptionMessages.CAMPO_NOMBRE_INVALIDO)
                            )
                    )
                    .verify();
        }
    }

    @Nested
    @DisplayName("Validación de apellido")
    class ApellidoTests {

        @Test
        void apellidoNull() {
            User user = validUser().toBuilder().apellidos(null).build();

            StepVerifier.create(executor.validateAll(user))
                    .expectErrorSatisfies(t ->
                            expectArgumentExceptionWithMessages(
                                    t,
                                    List.of(ExceptionMessages.CAMPO_APELLIDO_INVALIDO)
                            )
                    )
                    .verify();
        }

        @Test
        void apellidoVacio() {
            User user = validUser().toBuilder().apellidos("").build();

            StepVerifier.create(executor.validateAll(user))
                    .expectErrorSatisfies(t ->
                            expectArgumentExceptionWithMessages(
                                    t,
                                    List.of(ExceptionMessages.CAMPO_APELLIDO_INVALIDO)
                            )
                    )
                    .verify();
        }
    }

    @Nested
    @DisplayName("Validación de salario")
    class SalarioTests {

        @Test
        void salarioNull() {
            User user = validUser().toBuilder().salarioBase(null).build();

            StepVerifier.create(executor.validateAll(user))
                    .expectErrorSatisfies(t ->
                            expectArgumentExceptionWithMessages(
                                    t,
                                    List.of(ExceptionMessages.CAMPO_SALARIO_INVALIDO)
                            )
                    )
                    .verify();
        }

        @Test
        void salarioNegativo() {
            User user = validUser().toBuilder().salarioBase(new BigDecimal("-1")).build();

            StepVerifier.create(executor.validateAll(user))
                    .expectErrorSatisfies(t ->
                            expectArgumentExceptionWithMessages(
                                    t,
                                    List.of(ExceptionMessages.CAMPO_SALARIO_INVALIDO)
                            )
                    )
                    .verify();
        }

        @Test
        void salarioMayorQueMaximo() {
            User user = validUser().toBuilder().salarioBase(new BigDecimal("15000001")).build();

            StepVerifier.create(executor.validateAll(user))
                    .expectErrorSatisfies(t ->
                            expectArgumentExceptionWithMessages(
                                    t,
                                    List.of(ExceptionMessages.CAMPO_SALARIO_INVALIDO)
                            )
                    )
                    .verify();
        }
    }

    @Nested
    @DisplayName("Validación de email")
    class EmailTests {

        @Test
        void emailNull() {
            User user = validUser().toBuilder().correoElectronico(null).build();

            StepVerifier.create(executor.validateAll(user))
                    .expectErrorSatisfies(t ->
                            expectArgumentExceptionWithMessages(
                                    t,
                                    List.of(ExceptionMessages.CAMPO_EMAIL_INVALIDO)
                            )
                    )
                    .verify();
        }

        @Test
        void emailVacio() {
            User user = validUser().toBuilder().correoElectronico("").build();

            StepVerifier.create(executor.validateAll(user))
                    .expectErrorSatisfies(t ->
                            expectArgumentExceptionWithMessages(
                                    t,
                                    List.of(ExceptionMessages.CAMPO_EMAIL_INVALIDO)
                            )
                    )
                    .verify();
        }

        @Test
        void emailFormatoInvalido() {
            User user = validUser().toBuilder().correoElectronico("bad@").build();

            StepVerifier.create(executor.validateAll(user))
                    .expectErrorSatisfies(t ->
                            expectArgumentExceptionWithMessages(
                                    t,
                                    List.of(ExceptionMessages.FORMATO_EMAIL_INVALIDO)
                            )
                    )
                    .verify();
        }

        @Test
        void emailFormatoValido() {
            User user = validUser().toBuilder().correoElectronico("user.name+alias@dominio.co").build();

            StepVerifier.create(executor.validateAll(user))
                    .verifyComplete();
        }
    }

    @Test
    @DisplayName("Múltiples errores: verifica agregación y orden (concat)")
    void multiplesErroresOrdenados() {

        User user = validUser().toBuilder()
                .nombres("")
                .apellidos(null)
                .correoElectronico("bad@")
                .build();

        StepVerifier.create(executor.validateAll(user))
                .expectErrorSatisfies(t ->
                        expectArgumentExceptionWithMessages(
                                t,
                                List.of(
                                        ExceptionMessages.CAMPO_NOMBRE_INVALIDO,
                                        ExceptionMessages.CAMPO_APELLIDO_INVALIDO,
                                        ExceptionMessages.FORMATO_EMAIL_INVALIDO
                                )
                        )
                )
                .verify();
    }
}
