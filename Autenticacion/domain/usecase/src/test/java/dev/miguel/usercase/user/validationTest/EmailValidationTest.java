package dev.miguel.usercase.user.validationTest;

import dev.miguel.model.user.User;
import dev.miguel.model.user.gateways.UserRepository;
import dev.miguel.usecase.user.validation.EmailValidation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
class EmailValidationTest {

    @InjectMocks
    private EmailValidation emailValidation;

    @Mock
    private UserRepository userRepository;

    private final User userSucess = User.builder()
            .id(1L)
            .nombres("Miguel")
            .apellidos("Mosquera")
            .fechaNacimiento(LocalDate.of(1996, 7, 12))
            .direccion("Cll 15 # 17a - 115")
            .telefono("+573225864404")
            .correoElectronico("mandresmosquera@gmail.com")
            .salarioBase(BigDecimal.valueOf(10000000))
            .build();


    @Test
    void whenEmailIsNull_shouldError() {
        User user = new User();
        user.setCorreoElectronico(null);

        StepVerifier.create(emailValidation.validate(user))
                .expectErrorMatches(t -> t instanceof IllegalArgumentException &&
                        t.getMessage().equals("El campo correo_electronico no puede ser nulo o vacío"))
                .verify();
    }

    @Test
    void whenEmailIsEmpty_shouldError() {
        User user = new User();
        user.setCorreoElectronico("");

        StepVerifier.create(emailValidation.validate(user))
                .expectErrorMatches(t -> t instanceof IllegalArgumentException &&
                        t.getMessage().equals("El campo correo_electronico no puede ser nulo o vacío"))
                .verify();
    }

    @Test
    void whenEmailHasInvalidFormat_shouldError() {
        User user = new User();
        user.setCorreoElectronico("no-es-un-email");

        StepVerifier.create(emailValidation.validate(user))
                .expectErrorMatches(t -> t instanceof IllegalArgumentException &&
                        t.getMessage().equals("El campo correo_electronico debe tener un formato de email válido"))
                .verify();

    }
}
