package dev.miguel.usercase.user;

import dev.miguel.model.rol.gateways.RolRepository;
import dev.miguel.model.user.User;
import dev.miguel.model.user.gateways.UserRepository;
import dev.miguel.model.exception.BusinessException;
import dev.miguel.usecase.user.UserUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserUseCaseTest {

    @InjectMocks
    UserUseCase useCase;

    @Mock
    UserRepository userRepository;
    @Mock
    RolRepository rolRepository;

    // Helpers
    private User validUser() {
        return User.builder()
                .nombres("Miguel")
                .apellidos("Mosquera")
                .salarioBase(new BigDecimal("2500000"))
                .correoElectronico("miguel@test.com")
                .contrasenia("12345")
                .rolId(1L)
                .build();
    }

    @Test
    void createUser_ok() {
        var input = validUser();

        when(userRepository.findUserByEmail(input.getCorreoElectronico()))
                .thenReturn(Mono.empty());

        when(rolRepository.existsById(input.getRolId()))
                .thenReturn(Mono.just(true));

        when(userRepository.saveUser(input))
                .thenReturn(Mono.just(input));

        StepVerifier.create(useCase.createUser(input))
                .verifyComplete();
    }

    @Test
    void createUser_error_whenEmailExists_throwsBusinessException() {
        var input = validUser();
        var existing = validUser();

        when(userRepository.findUserByEmail(input.getCorreoElectronico()))
                .thenReturn(Mono.just(existing));

        var result = useCase.createUser(input);

        StepVerifier.create(result)
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void createUser_error_whenRolDoesntExists_throwsBusinessException() {
        var input = validUser();

        when(userRepository.findUserByEmail(input.getCorreoElectronico()))
                .thenReturn(Mono.empty());

        when(rolRepository.existsById(input.getRolId()))
                .thenReturn(Mono.just(false));

        var result = useCase.createUser(input);

        StepVerifier.create(result)
                .expectError(BusinessException.class)
                .verify();
    }

}
